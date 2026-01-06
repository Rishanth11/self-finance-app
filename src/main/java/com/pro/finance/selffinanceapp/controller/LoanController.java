package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.model.Loan;
import com.pro.finance.selffinanceapp.service.EmiService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
public class LoanController {

    private final EmiService emiService;

    public LoanController(EmiService emiService) {
        this.emiService = emiService;
    }

    // ➕ ADD LOAN
    @PostMapping
    public Loan addLoan(@RequestBody Loan loan) {
        return emiService.addLoan(loan);
    }
}
