package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.SipPortfolioDTO;
import com.pro.finance.selffinanceapp.model.SipInvestment;
import com.pro.finance.selffinanceapp.model.SipTransaction;
import com.pro.finance.selffinanceapp.repository.SipInvestmentRepository;
import com.pro.finance.selffinanceapp.repository.SipTransactionRepository;
import com.pro.finance.selffinanceapp.util.XirrCalculator;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

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
        this.sipRepo = sipRepo;
        this.txnRepo = txnRepo;
        this.navService = navService;
    }

    /* ---------------- ADD SIP ---------------- */

    public SipInvestment saveSip(SipInvestment sipInvestment) {
        return sipRepo.save(sipInvestment);
    }

    /* ---------------- SIP CHART (IMPROVED) ---------------- */

    public List<Map<String, Object>> getSipChart(Long sipId) {

        sipRepo.findById(sipId)
                .orElseThrow(() -> new RuntimeException("SIP not found with id: " + sipId));

        List<SipTransaction> txns = txnRepo.findBySipId(sipId);
        List<Map<String, Object>> result = new ArrayList<>();

        double cumulativeInvested = 0;
        double cumulativeUnits = 0;

        for (SipTransaction txn : txns) {
            cumulativeInvested += txn.getAmount();
            cumulativeUnits += txn.getUnits();

            Map<String, Object> map = new HashMap<>();
            map.put("date", txn.getInvestDate());
            map.put("invested", cumulativeInvested);
            map.put("value", cumulativeUnits * txn.getNav());

            result.add(map);
        }
        return result;
    }

    /* ---------------- SIP PORTFOLIO (WITH XIRR) ---------------- */

    public SipPortfolioDTO getPortfolio(Long sipId) {

        SipInvestment sip = sipRepo.findById(sipId)
                .orElseThrow(() -> new RuntimeException("SIP not found with id: " + sipId));

        List<SipTransaction> txns = txnRepo.findBySipId(sipId);

        double totalInvested = 0;
        double totalUnits = 0;

        for (SipTransaction txn : txns) {
            totalInvested += txn.getAmount();
            totalUnits += txn.getUnits();
        }

        double currentNav;
        boolean navAvailable = true;

        try {
            currentNav = navService.fetchLatestNav(sip.getFundCode());
        } catch (Exception e) {
            navAvailable = false;
            currentNav = getLastKnownNav(txns);
        }

        double currentValue = totalUnits * currentNav;

        // ---------------- XIRR (SAFE & OPTIONAL) ----------------
        double xirr = 0;
        if (txns.size() >= 2 && currentValue > 0) {

            List<Double> cashFlows = new ArrayList<>();
            List<LocalDate> dates = new ArrayList<>();

            for (SipTransaction txn : txns) {
                cashFlows.add(-txn.getAmount());
                dates.add(txn.getInvestDate());
            }

            cashFlows.add(currentValue);
            dates.add(LocalDate.now());

            try {
                xirr = XirrCalculator.calculate(cashFlows, dates);
            } catch (Exception ignored) {
                xirr = 0;
            }
        }

        SipPortfolioDTO dto = new SipPortfolioDTO();
        dto.setTotalInvested(totalInvested);
        dto.setCurrentValue(currentValue);
        dto.setReturns(currentValue - totalInvested);
        dto.setXirr(xirr);
        dto.setNavAvailable(navAvailable);

        return dto;
    }

    /* ---------------- AUTO SIP EXECUTION ---------------- */

    @Scheduled(cron = "0 0 1 * * ?") // daily 1 AM
    public void autoSipExecution() {

        LocalDate today = LocalDate.now();
        List<SipInvestment> sips = sipRepo.findAll();

        for (SipInvestment sip : sips) {

            if (!sip.isActive()) continue;
            if (sip.getSipDay() != today.getDayOfMonth()) continue;

            boolean alreadyExecuted =
                    txnRepo.existsBySipIdAndInvestDate(sip.getId(), today);

            if (alreadyExecuted) continue;

            double nav;
            try {
                nav = navService.fetchLatestNav(sip.getFundCode());
            } catch (Exception e) {
                continue;
            }

            if (nav <= 0) continue;

            double units = sip.getMonthlyAmount() / nav;

            SipTransaction txn = new SipTransaction();
            txn.setSipId(sip.getId());
            txn.setInvestDate(today);
            txn.setNav(nav);
            txn.setUnits(units);
            txn.setAmount(sip.getMonthlyAmount());

            txnRepo.save(txn);
        }
    }

    private double getLastKnownNav(List<SipTransaction> txns) {
        if (txns.isEmpty()) return 0;
        return txns.get(txns.size() - 1).getNav();
    }

    public void executeSipNow(Long sipId) {

        SipInvestment sip = sipRepo.findById(sipId)
                .orElseThrow(() -> new RuntimeException("SIP not found"));

        double nav = 100; // mock NAV for now
        double units = sip.getMonthlyAmount() / nav;

        SipTransaction txn = new SipTransaction();
        txn.setSipId(sip.getId());
        txn.setInvestDate(LocalDate.now());
        txn.setNav(nav);
        txn.setUnits(units);
        txn.setAmount(sip.getMonthlyAmount());

        txnRepo.save(txn);
    }

    public List<SipInvestment> getSipByUser(Long userId) {
        return sipRepo.findByUserId(userId);
    }

    public void deleteSip(Long sipId) {
        sipRepo.deleteById(sipId);
    }

    public void deactivateSip(Long sipId) {
        SipInvestment sip = sipRepo.findById(sipId)
                .orElseThrow(() -> new RuntimeException("SIP not found"));
        sip.setActive(false);
        sipRepo.save(sip);
    }

    public void activateSip(Long sipId) {
        SipInvestment sip = sipRepo.findById(sipId)
                .orElseThrow(() -> new RuntimeException("SIP not found"));
        sip.setActive(true);
        sipRepo.save(sip);
    }


}
