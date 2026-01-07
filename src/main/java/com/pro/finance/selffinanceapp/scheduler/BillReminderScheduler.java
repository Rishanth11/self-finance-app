package com.pro.finance.selffinanceapp.scheduler;

import com.pro.finance.selffinanceapp.model.RecurringBill;
import com.pro.finance.selffinanceapp.repository.RecurringBillRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class BillReminderScheduler {

    private final RecurringBillRepository repo;

    public BillReminderScheduler(RecurringBillRepository repo) {
        this.repo = repo;
    }

    // Runs every day at 9 AM
    @Scheduled(cron = "0 0 9 * * *")
    public void sendBillReminders() {

        LocalDate today = LocalDate.now();
        LocalDate upcoming = today.plusDays(3);

        List<RecurringBill> bills =
                repo.findByNextDueDateBetween(today, upcoming);

        bills.forEach(bill -> {
            System.out.println(
                    "🔔 Reminder: " + bill.getBillName() +
                            " due on " + bill.getNextDueDate()
            );
        });
    }
}

