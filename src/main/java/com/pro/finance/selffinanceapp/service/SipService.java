package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.SipPortfolioDTO;
import com.pro.finance.selffinanceapp.model.SipInvestment;
import com.pro.finance.selffinanceapp.model.SipTransaction;
import com.pro.finance.selffinanceapp.repository.SipInvestmentRepository;
import com.pro.finance.selffinanceapp.repository.SipTransactionRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public SipInvestment saveSip(SipInvestment sipInvestment) {
        return sipRepo.save(sipInvestment);
    }

    /* ---------------- SIP CHART ---------------- */

    public List<Map<String, Object>> getSipChart(Long sipId) {

        List<SipTransaction> txns = txnRepo.findBySipId(sipId);
        List<Map<String, Object>> result = new ArrayList<>();

        for (SipTransaction txn : txns) {
            Map<String, Object> map = new HashMap<>();
            map.put("date", txn.getInvestDate());
            map.put("invested", txn.getAmount());
            map.put("units", txn.getUnits());
            result.add(map);
        }
        return result;
    }

    /* ---------------- SIP PORTFOLIO ---------------- */

    public SipPortfolioDTO getPortfolio(Long sipId) {

        List<SipTransaction> txns = txnRepo.findBySipId(sipId);
        SipInvestment sip = sipRepo.findById(sipId).orElseThrow();

        double totalInvested = 0;
        double totalUnits = 0;

        for (SipTransaction txn : txns) {
            totalInvested += txn.getAmount();
            totalUnits += txn.getUnits();
        }

        double currentNav = navService.fetchLatestNav(sip.getFundCode());
        double currentValue = totalUnits * currentNav;

        SipPortfolioDTO dto = new SipPortfolioDTO();
        dto.setTotalInvested(totalInvested);
        dto.setCurrentValue(currentValue);
        dto.setReturns(currentValue - totalInvested);
        dto.setXirr(0); // will implement next

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

            double nav = navService.fetchLatestNav(sip.getFundCode());
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
}
