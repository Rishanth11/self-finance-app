package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.dto.RecurringBillDTO;
import com.pro.finance.selffinanceapp.model.RecurringBill;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.service.RecurringBillService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bills")
public class RecurringBillController {

    private final RecurringBillService service;

    public RecurringBillController(RecurringBillService service) {
        this.service = service;
    }

    // ➕ ADD BILL
    @PostMapping
    public RecurringBill addBill(
            @RequestBody RecurringBillDTO dto,
            @AuthenticationPrincipal User user) {

        return service.addBill(dto, user);
    }

    // ✅ MARK AS PAID
    @PutMapping("/{id}/pay")
    public RecurringBill payBill(@PathVariable Long id) {
        return service.markAsPaid(id);
    }
}
