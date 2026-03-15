package com.pro.finance.selffinanceapp.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@RestController
@RequestMapping("/api/stocks/proxy")
public class StockPriceController {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * GET /api/stocks/proxy/price?symbol=RELIANCE.NS
     * Returns raw Yahoo Finance JSON — frontend parses meta itself.
     */
    @GetMapping("/price")
    public ResponseEntity<?> getPrice(@RequestParam String symbol) {
        try {
            String url = "https://query1.finance.yahoo.com/v8/finance/chart/"
                    + symbol + "?interval=1d&range=1d";

            HttpHeaders headers = buildHeaders();
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            return ResponseEntity.status(502)
                    .body(Map.of("error", "Failed to fetch: " + symbol + " — " + e.getMessage()));
        }
    }

    /**
     * GET /api/stocks/proxy/prices?symbols=RELIANCE.NS,TCS.NS
     * Batch fetch — returns { "RELIANCE.NS": { price, change, changePct, prevClose }, ... }
     * Uses safe casting to avoid ClassCastException with nested LinkedHashMaps.
     */
    @GetMapping("/prices")
    public ResponseEntity<?> getPrices(@RequestParam String symbols) {
        String[] symbolArr = symbols.split(",");
        Map<String, Object> result = new LinkedHashMap<>();

        for (String raw : symbolArr) {
            String symbol = raw.trim();
            if (symbol.isEmpty()) continue;

            try {
                String url = "https://query1.finance.yahoo.com/v8/finance/chart/"
                        + symbol + "?interval=1d&range=1d";

                HttpHeaders headers = buildHeaders();
                ResponseEntity<Map> response = restTemplate.exchange(
                        url, HttpMethod.GET, new HttpEntity<>(headers), Map.class);

                Map<String, Object> priceData = extractPriceData(response.getBody());
                if (priceData != null) {
                    result.put(symbol, priceData);
                } else {
                    result.put(symbol, Map.of("error", "No data"));
                }

            } catch (Exception e) {
                result.put(symbol, Map.of("error", e.getMessage()));
            }
        }

        return ResponseEntity.ok(result);
    }

    /**
     * Safely extracts price/change/changePct from Yahoo Finance v8 response.
     * Handles nested LinkedHashMap safely without hard casts.
     */
    private Map<String, Object> extractPriceData(Map<?, ?> body) {
        try {
            if (body == null) return null;

            Object chartObj = body.get("chart");
            if (!(chartObj instanceof Map)) return null;
            Map<?, ?> chart = (Map<?, ?>) chartObj;

            Object resultObj = chart.get("result");
            if (!(resultObj instanceof List)) return null;
            List<?> resultList = (List<?>) resultObj;

            if (resultList.isEmpty()) return null;
            Object firstObj = resultList.get(0);
            if (!(firstObj instanceof Map)) return null;
            Map<?, ?> first = (Map<?, ?>) firstObj;

            Object metaObj = first.get("meta");
            if (!(metaObj instanceof Map)) return null;
            Map<?, ?> meta = (Map<?, ?>) metaObj;

            double price     = toDouble(meta.get("regularMarketPrice"));
            double prevClose = toDouble(meta.get("previousClose"));

            // fallback for prevClose
            if (prevClose == 0) prevClose = toDouble(meta.get("chartPreviousClose"));
            if (prevClose == 0) prevClose = price;

            double change    = price - prevClose;
            double changePct = prevClose > 0 ? (change / prevClose) * 100.0 : 0.0;

            Map<String, Object> out = new LinkedHashMap<>();
            out.put("price",     Math.round(price * 100.0) / 100.0);
            out.put("change",    Math.round(change * 100.0) / 100.0);
            out.put("changePct", Math.round(changePct * 100.0) / 100.0);
            out.put("prevClose", Math.round(prevClose * 100.0) / 100.0);
            return out;

        } catch (Exception e) {
            return null;
        }
    }

    /**
     * GET /api/stocks/proxy/news
     * Fetches Economic Times Markets RSS, parses XML server-side, returns JSON.
     */
    @GetMapping("/news")
    public ResponseEntity<?> getNews() {
        try {
            String rssUrl = "https://economictimes.indiatimes.com/markets/rssfeeds/1977021501.cms";

            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0");
            headers.set("Accept", "application/rss+xml, application/xml, text/xml");

            ResponseEntity<String> response = restTemplate.exchange(
                    rssUrl, HttpMethod.GET, new HttpEntity<>(headers), String.class);

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

                    if (desc    != null) desc    = desc.replaceAll("<[^>]+>", "").trim();
                    if (title   != null) title   = title.replaceAll("<[^>]+>", "")
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
        h.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
        h.set("Accept", "application/json, text/plain, */*");
        h.set("Accept-Language", "en-US,en;q=0.9");
        h.set("Referer", "https://finance.yahoo.com");
        return h;
    }

    private String extractTag(String xml, String tag) {
        String open  = "<" + tag + ">";
        String close = "</" + tag + ">";
        int start = xml.indexOf(open);
        int end   = xml.indexOf(close);
        if (start == -1 || end == -1) return null;
        return xml.substring(start + open.length(), end).trim()
                .replace("<![CDATA[", "").replace("]]>", "").trim();
    }

    private double toDouble(Object val) {
        if (val == null) return 0.0;
        try { return Double.parseDouble(val.toString()); }
        catch (NumberFormatException e) { return 0.0; }
    }
}