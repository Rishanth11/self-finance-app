package com.pro.finance.selffinanceapp.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

/**
 * Stock price proxy — all requests go through Yahoo Finance.
 *
 * Index → ETF ticker mapping (ETF price × multiplier ≈ index level):
 *   NIFTY50    → NIFTYBEES.NS   × 100  ✅ working
 *   SENSEX     → BSLSENSEX.NS   × 100  (Aditya Birla Sun Life Sensex ETF)
 *                SETFNN50.NS    × 100  (fallback: SBI ETF Nifty 50)
 *                MOM100.NS      × 100  (second fallback)
 *   BANKNIFTY  → BANKBEES.NS    × 100  ✅ working
 *   NIFTYIT    → NIFTYIT.NS     × 1    ✅ working
 */
@RestController
@RequestMapping("/api/stocks/proxy")
public class StockPriceController {

    private final RestTemplate restTemplate = new RestTemplate();

    // Index key → ordered list of ETF tickers to try (first one that returns data wins)
    private static final Map<String, String[]> INDEX_ETF_FALLBACKS = new LinkedHashMap<>();
    // multiplier is always 100 for ETFs (ETF price ≈ index / 100)
    private static final Map<String, Double> INDEX_MULTIPLIER = new LinkedHashMap<>();

    static {
        // Nifty 50
        INDEX_ETF_FALLBACKS.put("NIFTY50",   new String[]{"NIFTYBEES.NS", "ICICIB22.NS", "SETFNIF50.NS"});
        INDEX_MULTIPLIER.put("NIFTY50",   100.0);

        // Sensex — try multiple tickers
        INDEX_ETF_FALLBACKS.put("SENSEX",    new String[]{"BSLSENSEX.NS", "SETFNN50.NS", "SENSEXIETF.NS", "ICICINIFTY.NS"});
        INDEX_MULTIPLIER.put("SENSEX",    100.0);

        // Bank Nifty
        INDEX_ETF_FALLBACKS.put("BANKNIFTY", new String[]{"BANKBEES.NS", "ITBEES.NS"});
        INDEX_MULTIPLIER.put("BANKNIFTY", 100.0);

        // Nifty IT — direct index ticker, no ETF needed
        INDEX_ETF_FALLBACKS.put("NIFTYIT",   new String[]{"NIFTYIT.NS"});
        INDEX_MULTIPLIER.put("NIFTYIT",   1.0);
    }

    // ── GET /api/stocks/proxy/price?symbol=RELIANCE.NS ──
    @GetMapping("/price")
    public ResponseEntity<?> getPrice(@RequestParam String symbol) {
        try {
            Map<String, Object> data = fetchWithFallback(new String[]{symbol}, 1.0);
            if (data == null) return ResponseEntity.status(502).body(Map.of("error", "No data for " + symbol));
            return ResponseEntity.ok(data);
        } catch (Exception e) {
            return ResponseEntity.status(502).body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * GET /api/stocks/proxy/prices?symbols=RELIANCE.NS,TCS.NS,NIFTY50,SENSEX,BANKNIFTY,NIFTYIT
     */
    @GetMapping("/prices")
    public ResponseEntity<?> getPrices(@RequestParam String symbols) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (String raw : symbols.split(",")) {
            String key = raw.trim();
            if (key.isEmpty()) continue;

            if (INDEX_ETF_FALLBACKS.containsKey(key)) {
                // Index — try ETF tickers in order until one works
                String[] tickers    = INDEX_ETF_FALLBACKS.get(key);
                double   multiplier = INDEX_MULTIPLIER.getOrDefault(key, 1.0);
                Map<String, Object> data = fetchWithFallback(tickers, multiplier);
                result.put(key, data != null ? data : Map.of("error", "No data"));
            } else {
                // Regular stock ticker
                Map<String, Object> data = fetchWithFallback(new String[]{key}, 1.0);
                result.put(key, data != null ? data : Map.of("error", "No data"));
            }
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Try each ticker in order — return first successful result.
     */
    private Map<String, Object> fetchWithFallback(String[] tickers, double multiplier) {
        for (String ticker : tickers) {
            Map<String, Object> data = fetchFromYahoo(ticker, multiplier);
            if (data != null) return data;
        }
        return null;
    }

    /**
     * Fetch price from Yahoo Finance v8 chart API (tries query1, then query2).
     */
    private Map<String, Object> fetchFromYahoo(String ticker, double multiplier) {
        for (String host : new String[]{"query1", "query2"}) {
            try {
                String encodedTicker = ticker.replace("^", "%5E");
                String url = "https://" + host + ".finance.yahoo.com/v8/finance/chart/"
                        + encodedTicker + "?interval=1d&range=1d";

                ResponseEntity<Map> resp = restTemplate.exchange(
                        url, HttpMethod.GET, new HttpEntity<>(buildHeaders()), Map.class);

                Map<String, Object> data = extractPriceData(resp.getBody(), multiplier);
                if (data != null) return data;

            } catch (Exception ignored) {}
        }
        return null;
    }

    /** Safely parse Yahoo Finance v8 meta block → { price, change, changePct, prevClose } */
    private Map<String, Object> extractPriceData(Map<?, ?> body, double multiplier) {
        try {
            if (body == null) return null;

            Object chartObj = body.get("chart");
            if (!(chartObj instanceof Map)) return null;
            Map<?, ?> chart = (Map<?, ?>) chartObj;
            if (chart.get("error") != null) return null;

            Object resultObj = chart.get("result");
            if (!(resultObj instanceof List)) return null;
            List<?> resultList = (List<?>) resultObj;
            if (resultList.isEmpty()) return null;

            Object firstObj = resultList.get(0);
            if (!(firstObj instanceof Map)) return null;

            Object metaObj = ((Map<?, ?>) firstObj).get("meta");
            if (!(metaObj instanceof Map)) return null;
            Map<?, ?> meta = (Map<?, ?>) metaObj;

            double rawPrice = toDouble(meta.get("regularMarketPrice"));
            if (rawPrice == 0) return null;

            double rawPrev = toDouble(meta.get("previousClose"));
            if (rawPrev == 0) rawPrev = toDouble(meta.get("chartPreviousClose"));
            if (rawPrev == 0) rawPrev = rawPrice;

            double price     = rawPrice * multiplier;
            double prevClose = rawPrev  * multiplier;
            double change    = price - prevClose;
            double changePct = prevClose > 0 ? (change / prevClose) * 100.0 : 0.0;

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("price",     round2(price));
            out.put("change",    round2(change));
            out.put("changePct", round2(changePct));
            out.put("prevClose", round2(prevClose));
            return out;

        } catch (Exception e) {
            return null;
        }
    }

    // ── NEWS ──
    @GetMapping("/news")
    public ResponseEntity<?> getNews() {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            headers.set("Accept", "application/rss+xml, application/xml, text/xml");

            ResponseEntity<String> response = restTemplate.exchange(
                    "https://economictimes.indiatimes.com/markets/rssfeeds/1977021501.cms",
                    HttpMethod.GET, new HttpEntity<>(headers), String.class);

            String xml = response.getBody();
            List<Map<String, String>> items = new ArrayList<>();

            if (xml != null) {
                String[] parts = xml.split("<item>");
                for (int i = 1; i < parts.length && items.size() < 20; i++) {
                    String item    = parts[i];
                    String title   = extractTag(item, "title");
                    String link    = extractTag(item, "link");
                    String pubDate = extractTag(item, "pubDate");
                    String desc    = extractTag(item, "description");

                    if (desc  != null) desc  = desc.replaceAll("<[^>]+>", "").trim();
                    if (title != null) title = title.replaceAll("<[^>]+>", "")
                            .replace("&amp;", "&").replace("&lt;", "<")
                            .replace("&gt;", ">").replace("&#39;", "'").trim();

                    if (title != null && !title.isEmpty() && link != null) {
                        Map<String, String> entry = new LinkedHashMap<>();
                        entry.put("title",       title);
                        entry.put("link",        link);
                        entry.put("pubDate",     pubDate != null ? pubDate : "");
                        entry.put("description", desc    != null ? desc    : "");
                        items.add(entry);
                    }
                }
            }
            return ResponseEntity.ok(Map.of("items", items));

        } catch (Exception e) {
            return ResponseEntity.status(502)
                    .body(Map.of("error", "Failed to fetch news: " + e.getMessage()));
        }
    }

    // ── HELPERS ──
    private HttpHeaders buildHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
        h.set("Accept", "application/json, text/plain, */*");
        h.set("Accept-Language", "en-US,en;q=0.9");
        h.set("Referer", "https://finance.yahoo.com/");
        return h;
    }

    private String extractTag(String xml, String tag) {
        String open  = "<" + tag + ">";
        String close = "</" + tag + ">";
        int start = xml.indexOf(open);
        int end   = xml.indexOf(close);
        if (start == -1 || end == -1) return null;
        return xml.substring(start + open.length(), end)
                .replace("<![CDATA[", "").replace("]]>", "").trim();
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        try { return Double.parseDouble(val.toString()); }
        catch (NumberFormatException e) { return 0.0; }
    }

    private double round2(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}