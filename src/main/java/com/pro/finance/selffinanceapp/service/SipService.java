package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.SipPortfolioDTO;
import com.pro.finance.selffinanceapp.dto.SipRequestDTO;
import com.pro.finance.selffinanceapp.model.*;
import com.pro.finance.selffinanceapp.repository.*;
import com.pro.finance.selffinanceapp.util.XirrCalculator;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

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
        this.sipRepo = sipRepo;
        this.txnRepo = txnRepo;
        this.navService = navService;
    }

    /* ---------------- CREATE SIP ---------------- */

    public SipInvestment createSip(SipRequestDTO dto, User user) {

        SipInvestment sip = new SipInvestment();
        sip.setUser(user);
        sip.setFundName(dto.getFundName());
        sip.setFundCode(dto.getFundCode());
        sip.setMonthlyAmount(dto.getMonthlyAmount());
        sip.setStartDate(dto.getStartDate());
        sip.setSipDay(dto.getSipDay());
        sip.setGoalName(dto.getGoalName());
        sip.setTargetAmount(dto.getTargetAmount());
        sip.setInflationRate(dto.getInflationRate());
        sip.setActive(true);

        return sipRepo.save(sip);
    }

    /* ---------------- EXECUTE SIP ---------------- */

    @Transactional
    public void executeSipNow(Long sipId, User user) {

        SipInvestment sip = getOwnedSip(sipId, user);

        boolean alreadyExecuted =
                txnRepo.existsBySipAndInvestDate(sip, LocalDate.now());

        if (alreadyExecuted) {
            throw new RuntimeException("SIP already executed today");
        }

        BigDecimal nav = navService.fetchLatestNav(sip.getFundCode());

        if (nav.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Invalid NAV");
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

    /* ---------------- PORTFOLIO ---------------- */

    public SipPortfolioDTO getPortfolio(Long sipId, User user) {

        SipInvestment sip = getOwnedSip(sipId, user);
        List<SipTransaction> txns = txnRepo.findBySip(sip);

        BigDecimal totalInvested = BigDecimal.ZERO;
        BigDecimal totalUnits = BigDecimal.ZERO;

        for (SipTransaction txn : txns) {
            totalInvested = totalInvested.add(txn.getAmount());
            totalUnits = totalUnits.add(txn.getUnits());
        }

        BigDecimal currentNav;
        boolean navAvailable = true;

        try {
            currentNav = navService.fetchLatestNav(sip.getFundCode());
        } catch (Exception e) {
            navAvailable = false;
            currentNav = BigDecimal.ZERO;
        }

        BigDecimal currentValue = totalUnits.multiply(currentNav);
        BigDecimal returns = currentValue.subtract(totalInvested);

        /* ---------------- XIRR ---------------- */

        BigDecimal xirr = BigDecimal.ZERO;

        if (txns.size() >= 1 && currentValue.compareTo(BigDecimal.ZERO) > 0) {

            List<Double> amounts = new ArrayList<>();
            List<LocalDate> dates = new ArrayList<>();

            for (SipTransaction txn : txns) {
                amounts.add(-txn.getAmount().doubleValue()); // investment = negative
                dates.add(txn.getInvestDate());
            }

            amounts.add(currentValue.doubleValue()); // current value = positive
            dates.add(LocalDate.now());

            double xirrValue = XirrCalculator.calculate(amounts, dates);

            xirr = BigDecimal.valueOf(xirrValue)
                    .setScale(2, RoundingMode.HALF_UP);
        }

        /* ---------------- REAL RETURN ---------------- */

        BigDecimal realReturn = BigDecimal.ZERO;

        if (sip.getInflationRate() != null) {
            realReturn = xirr.subtract(sip.getInflationRate())
                    .setScale(2, RoundingMode.HALF_UP);
        }

        /* ---------------- GOAL PROGRESS ---------------- */

        BigDecimal goalProgress = BigDecimal.ZERO;

        if (sip.getTargetAmount() != null &&
                sip.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {

            goalProgress = currentValue
                    .divide(sip.getTargetAmount(), 6, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return new SipPortfolioDTO(
                totalInvested,
                currentValue,
                returns,
                xirr,
                realReturn,
                goalProgress,
                navAvailable,
                sip.getGoalName(),
                sip.getTargetAmount()
        );
    }

    /* ---------------- OWNERSHIP VALIDATION ---------------- */

    private SipInvestment getOwnedSip(Long sipId, User user) {

        SipInvestment sip = sipRepo.findById(sipId)
                .orElseThrow(() -> new RuntimeException("SIP not found"));

        if (!sip.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        return sip;
    }

    public List<SipInvestment> getAllByUser(User user) {
        return sipRepo.findByUser(user);
    }

    public List<Map<String, Object>> getSipChart(Long sipId, User user) {

        SipInvestment sip = getOwnedSip(sipId, user);
        List<SipTransaction> txns = txnRepo.findBySip(sip);

        txns.sort(Comparator.comparing(SipTransaction::getInvestDate));

        List<Map<String, Object>> result = new ArrayList<>();

        BigDecimal cumulativeUnits = BigDecimal.ZERO;

        BigDecimal currentNav;
        try {
            currentNav = navService.fetchLatestNav(sip.getFundCode());
        } catch (Exception e) {
            currentNav = BigDecimal.ZERO;
        }

        for (SipTransaction txn : txns) {

            cumulativeUnits = cumulativeUnits.add(txn.getUnits());

            Map<String, Object> map = new HashMap<>();
            map.put("date", txn.getInvestDate().toString());
            map.put("value", cumulativeUnits.multiply(currentNav));

            result.add(map);
        }

        return result;
    }

    public void deleteSip(Long sipId, User user) {
        SipInvestment sip = getOwnedSip(sipId, user);
        sipRepo.delete(sip);
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


}