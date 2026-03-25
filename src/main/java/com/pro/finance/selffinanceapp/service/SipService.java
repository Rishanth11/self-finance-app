package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.SipPortfolioDTO;
import com.pro.finance.selffinanceapp.dto.SipRequestDTO;
import com.pro.finance.selffinanceapp.model.*;
import com.pro.finance.selffinanceapp.repository.*;
import com.pro.finance.selffinanceapp.util.XirrCalculator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class SipService {

    private final SipInvestmentRepository sipRepo;
    private final SipTransactionRepository txnRepo;
    private final NavService navService;

    public SipService(SipInvestmentRepository sipRepo,
                      SipTransactionRepository txnRepo,
                      NavService navService) {
        this.sipRepo    = sipRepo;
        this.txnRepo    = txnRepo;
        this.navService = navService;
    }

    /* ────────────────── CREATE SIP ────────────────── */

    public SipInvestment createSip(SipRequestDTO dto, User user) {
        SipInvestment sip = new SipInvestment();
        sip.setUser(user);
        sip.setFundName(dto.getFundName());
        sip.setFundCode(dto.getFundCode());   // may be null if user typed manually — that's OK
        sip.setMonthlyAmount(dto.getMonthlyAmount());
        sip.setStartDate(dto.getStartDate());
        sip.setSipDay(dto.getSipDay());
        sip.setGoalName(dto.getGoalName());
        sip.setTargetAmount(dto.getTargetAmount());
        sip.setInflationRate(dto.getInflationRate());
        sip.setActive(true);
        return sipRepo.save(sip);
    }

    /* ────────────────── EXECUTE SIP ────────────────── */

    /**
     * FIX: Previously threw RuntimeException("Invalid NAV") when fundCode was null
     * (user typed "HDFC" manually → fundCode never set → navService.fetchLatestNav(null) fails).
     * Now validates fundCode before attempting NAV fetch and returns a clear error message.
     */
    @Transactional
    public void executeSipNow(Long sipId, User user) {
        SipInvestment sip = getOwnedSip(sipId, user);

        // ── Guard: fundCode must be set for NAV lookup ─────────────────────────
        if (sip.getFundCode() == null || sip.getFundCode().isBlank()) {
            throw new RuntimeException(
                    "Fund code not set for this SIP. Please edit the SIP and select a fund " +
                            "from the search results to set the correct AMFI scheme code. " +
                            "Current fund name: '" + sip.getFundName() + "'"
            );
        }

        // ── Guard: already executed today ─────────────────────────────────────
        boolean alreadyExecuted = txnRepo.existsBySipAndInvestDate(sip, LocalDate.now());
        if (alreadyExecuted) {
            throw new RuntimeException("SIP already executed today for fund: " + sip.getFundName());
        }

        // ── Fetch NAV ─────────────────────────────────────────────────────────
        BigDecimal nav;
        try {
            nav = navService.fetchLatestNav(sip.getFundCode());
        } catch (Exception e) {
            throw new RuntimeException(
                    "Could not fetch NAV for fund code '" + sip.getFundCode() +
                            "'. The MFAPI service may be temporarily unavailable. Error: " + e.getMessage()
            );
        }

        if (nav == null || nav.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(
                    "Invalid NAV returned for fund '" + sip.getFundName() +
                            "' (code: " + sip.getFundCode() + "). Value: " + nav
            );
        }

        BigDecimal units = sip.getMonthlyAmount()
                .divide(nav, 6, RoundingMode.HALF_UP);

        SipTransaction txn = new SipTransaction();
        txn.setSip(sip);
        txn.setInvestDate(LocalDate.now());
        txn.setNav(nav);
        txn.setUnits(units);
        txn.setAmount(sip.getMonthlyAmount());

        txnRepo.save(txn);
    }

    /* ────────────────── PORTFOLIO ────────────────── */

    /**
     * FIX: Returns navAvailable=false with partial data when fundCode is null
     * instead of crashing. Dashboard handles navAvailable=false gracefully.
     */
    public SipPortfolioDTO getPortfolio(Long sipId, User user) {
        SipInvestment sip  = getOwnedSip(sipId, user);
        List<SipTransaction> txns = txnRepo.findBySip(sip);

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalUnits    = BigDecimal.ZERO;

        for (SipTransaction txn : txns) {
            totalInvested = totalInvested.add(txn.getAmount());
            totalUnits    = totalUnits.add(txn.getUnits());
        }

        // ── NAV fetch — graceful on missing fundCode or API failure ───────────
        BigDecimal currentNav;
        boolean    navAvailable = true;

        if (sip.getFundCode() == null || sip.getFundCode().isBlank()) {
            // User typed fund name manually — no code set, NAV unavailable
            navAvailable = false;
            currentNav   = BigDecimal.ZERO;
            System.out.println("⚠️ SIP id=" + sipId + " has no fundCode — NAV unavailable. " +
                    "Fund name: '" + sip.getFundName() + "'");
        } else {
            try {
                currentNav = navService.fetchLatestNav(sip.getFundCode());
                if (currentNav == null || currentNav.compareTo(BigDecimal.ZERO) <= 0) {
                    navAvailable = false;
                    currentNav   = BigDecimal.ZERO;
                }
            } catch (Exception e) {
                navAvailable = false;
                currentNav   = BigDecimal.ZERO;
                System.out.println("⚠️ NAV fetch failed for fundCode=" + sip.getFundCode() +
                        ": " + e.getMessage());
            }
        }

        BigDecimal currentValue = totalUnits.multiply(currentNav);
        BigDecimal returns      = currentValue.subtract(totalInvested);

        /* XIRR — only when we have a valid current value */
        BigDecimal xirr = BigDecimal.ZERO;
        if (navAvailable && txns.size() >= 1 && currentValue.compareTo(BigDecimal.ZERO) > 0) {
            try {
                List<Double>    amounts = new ArrayList<>();
                List<LocalDate> dates   = new ArrayList<>();
                for (SipTransaction txn : txns) {
                    amounts.add(-txn.getAmount().doubleValue());
                    dates.add(txn.getInvestDate());
                }
                amounts.add(currentValue.doubleValue());
                dates.add(LocalDate.now());
                double xirrValue = XirrCalculator.calculate(amounts, dates);
                xirr = BigDecimal.valueOf(xirrValue).setScale(2, RoundingMode.HALF_UP);
            } catch (Exception e) {
                System.out.println("⚠️ XIRR calculation failed: " + e.getMessage());
                xirr = BigDecimal.ZERO;
            }
        }

        /* Real return */
        BigDecimal realReturn = BigDecimal.ZERO;
        if (sip.getInflationRate() != null) {
            realReturn = xirr.subtract(sip.getInflationRate())
                    .setScale(2, RoundingMode.HALF_UP);
        }

        /* Goal progress */
        BigDecimal goalProgress = BigDecimal.ZERO;
        if (navAvailable &&
                sip.getTargetAmount() != null &&
                sip.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            goalProgress = currentValue
                    .divide(sip.getTargetAmount(), 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new SipPortfolioDTO(
                totalInvested, currentValue, returns,
                xirr, realReturn, goalProgress,
                navAvailable, sip.getGoalName(), sip.getTargetAmount()
        );
    }

    /* ────────────────── CHART ────────────────── */

    public List<Map<String, Object>> getSipChart(Long sipId, User user) {
        SipInvestment        sip  = getOwnedSip(sipId, user);
        List<SipTransaction> txns = txnRepo.findBySip(sip);
        txns.sort(Comparator.comparing(SipTransaction::getInvestDate));

        BigDecimal currentNav = BigDecimal.ZERO;
        if (sip.getFundCode() != null && !sip.getFundCode().isBlank()) {
            try {
                currentNav = navService.fetchLatestNav(sip.getFundCode());
                if (currentNav == null) currentNav = BigDecimal.ZERO;
            } catch (Exception e) {
                currentNav = BigDecimal.ZERO;
            }
        }

        List<Map<String, Object>> result = new ArrayList<>();
        BigDecimal cumulativeUnits = BigDecimal.ZERO;

        for (SipTransaction txn : txns) {
            cumulativeUnits = cumulativeUnits.add(txn.getUnits());

            Map<String, Object> map = new HashMap<>();
            map.put("date",  txn.getInvestDate().toString());
            map.put("value", cumulativeUnits.multiply(txn.getNav())
                    .setScale(2, RoundingMode.HALF_UP));
            result.add(map);
        }

        if (currentNav.compareTo(BigDecimal.ZERO) > 0 && !txns.isEmpty()) {
            Map<String, Object> today = new HashMap<>();
            today.put("date",  LocalDate.now().toString());
            today.put("value", cumulativeUnits.multiply(currentNav)
                    .setScale(2, RoundingMode.HALF_UP));
            result.add(today);
        }

        return result;
    }

    /* ────────────────── FUND SEARCH ────────────────── */

    /**
     * NEW: Search MFAPI.in for fund scheme codes.
     * Used by the frontend to let users pick the exact AMFI scheme code
     * instead of typing a free-text fund name.
     *
     * Returns list of: { schemeCode, schemeName }
     */
    public List<Map<String, Object>> searchFunds(String query) {
        try {
            String url = "https://api.mfapi.in/mf/search?q=" +
                    java.net.URLEncoder.encode(query, "UTF-8");

            RestTemplate rt = new RestTemplate();
            List body = rt.getForObject(url, List.class);

            if (body == null) return Collections.emptyList();

            List<Map<String, Object>> results = new ArrayList<>();
            int count = 0;
            for (Object item : body) {
                if (count++ >= 20) break;   // limit to 20 results
                if (item instanceof Map) {
                    Map m = (Map) item;
                    Map<String, Object> result = new HashMap<>();
                    result.put("schemeCode", m.get("schemeCode"));
                    result.put("schemeName", m.get("schemeName"));
                    results.add(result);
                }
            }
            return results;

        } catch (Exception e) {
            System.out.println("⚠️ Fund search failed for query='" + query + "': " + e.getMessage());
            return Collections.emptyList();
        }
    }

    /* ────────────────── CRUD ────────────────── */

    public List<SipInvestment> getAllByUser(User user) {
        return sipRepo.findByUser(user);
    }

    public void deleteSip(Long sipId, User user) {
        sipRepo.delete(getOwnedSip(sipId, user));
    }

    public void deactivateSip(Long sipId, User user) {
        SipInvestment sip = getOwnedSip(sipId, user);
        sip.setActive(false);
        sipRepo.save(sip);
    }

    public void activateSip(Long sipId, User user) {
        SipInvestment sip = getOwnedSip(sipId, user);
        sip.setActive(true);
        sipRepo.save(sip);
    }

    /* ────────────────── OWNERSHIP CHECK ────────────────── */

    private SipInvestment getOwnedSip(Long sipId, User user) {
        SipInvestment sip = sipRepo.findById(sipId)
                .orElseThrow(() -> new RuntimeException("SIP not found: id=" + sipId));
        if (!sip.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }
        return sip;
    }
}