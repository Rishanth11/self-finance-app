package com.pro.finance.selffinanceapp.scheduler;

import com.pro.finance.selffinanceapp.model.SipInvestment;
import com.pro.finance.selffinanceapp.repository.SipInvestmentRepository;
import com.pro.finance.selffinanceapp.service.SipService;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.util.List;

public class SipScheduler {
    private final SipInvestmentRepository sipRepo;
    private final SipService sipService;

    public SipScheduler(SipInvestmentRepository sipRepo,
                        SipService sipService) {
        this.sipRepo = sipRepo;
        this.sipService = sipService;
    }

    @Scheduled(cron = "0 0 10 * * ?")
    public void autoExecuteSips() {

        int today = LocalDate.now().getDayOfMonth();

        List<SipInvestment> activeSips =
                sipRepo.findAll()
                        .stream()
                        .filter(SipInvestment::isActive)
                        .toList();

        for (SipInvestment sip : activeSips) {

            if (sip.getSipDay() == today) {

                try {
                    sipService.executeSipNow(sip.getId(), sip.getUser());
                } catch (Exception ignored) {}
            }
        }
    }
}
