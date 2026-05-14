package com.rishanth.flux360.scheduler;

import com.rishanth.flux360.model.SipInvestment;
import com.rishanth.flux360.repository.SipInvestmentRepository;
import com.rishanth.flux360.service.SipService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component  // ← Fix 1: Spring now picks this up as a bean
public class SipScheduler {

    private final SipInvestmentRepository sipRepo;
    private final SipService sipService;

    public SipScheduler(SipInvestmentRepository sipRepo,
                        SipService sipService) {
        this.sipRepo = sipRepo;
        this.sipService = sipService;
    }

    @Scheduled(cron = "0 0 10 * * ?")  // runs every day at 10:00 AM
    public void autoExecuteSips() {

        int today = LocalDate.now().getDayOfMonth();

        // Fix 2: query only active SIPs — database filters, not Java stream
        List<SipInvestment> activeSips = sipRepo.findByActiveTrue();

        for (SipInvestment sip : activeSips) {
            if (sip.getSipDay() == today) {
                try {
                    sipService.executeSipNow(sip.getId(), sip.getUser());
                } catch (Exception ignored) {
                    // already executed today or invalid NAV — skip silently
                }
            }
        }
    }
}