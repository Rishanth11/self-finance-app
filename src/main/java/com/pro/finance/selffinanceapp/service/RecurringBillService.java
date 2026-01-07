package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.RecurringBillDTO;
import com.pro.finance.selffinanceapp.model.Frequency;
import com.pro.finance.selffinanceapp.model.RecurringBill;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.repository.RecurringBillRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;


@Service
public class RecurringBillService {

    private final RecurringBillRepository repo;

    public RecurringBillService(RecurringBillRepository repo) {
        this.repo = repo;
    }

    // ➕ ADD BILL
    public RecurringBill addBill(RecurringBillDTO dto, User user) {

        RecurringBill bill = new RecurringBill();
        bill.setBillName(dto.getBillName());
        bill.setAmount(dto.getAmount());
        bill.setFrequency(dto.getFrequency());
        bill.setStartDate(dto.getStartDate());
        bill.setUser(user);
        bill.setPaid(false);

        bill.setNextDueDate(
                calculateNextDueDate(
                        dto.getStartDate(),
                        dto.getFrequency()
                )
        );


        return repo.save(bill);
    }

    private LocalDate calculateNextDueDate(LocalDate date, Frequency freq) {
        return switch (freq) {
            case MONTHLY -> date.plusMonths(1);
            case QUARTERLY -> date.plusMonths(3);
            case ANNUAL -> date.plusYears(1);
        };
    }


    // ✅ MARK AS PAID
    public RecurringBill markAsPaid(Long billId) {

        RecurringBill bill = repo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found"));

        bill.setPaid(true);

        bill.setNextDueDate(
                calculateNextDueDate(
                        bill.getNextDueDate(),
                        bill.getFrequency()
                )
        );

        bill.setPaid(false); // reset for next cycle

        return repo.save(bill);
    }

}
