package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.model.EmiSchedule;
import com.pro.finance.selffinanceapp.model.Loan;
import com.pro.finance.selffinanceapp.repository.EmiScheduleRepository;
import com.pro.finance.selffinanceapp.repository.LoanRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmiService {

    private final LoanRepository loanRepo;
    private final EmiScheduleRepository emiRepo;

    public EmiService(LoanRepository loanRepo, EmiScheduleRepository emiRepo) {
        this.loanRepo = loanRepo;
        this.emiRepo = emiRepo;
    }

    // 1️⃣ ADD LOAN + AUTO EMI CALCULATION
    public Loan addLoan(Loan loan) {

        double r = loan.getInterestRate() / 12 / 100;
        int n = loan.getTenureMonths();
        double p = loan.getPrincipal();

        double emi = (p * r * Math.pow(1 + r, n)) /
                (Math.pow(1 + r, n) - 1);

        double totalPayable = emi * n;
        double totalInterest = totalPayable - p;

        loan.setEmiAmount(Math.round(emi));
        loan.setTotalPayable(Math.round(totalPayable));
        loan.setTotalInterest(Math.round(totalInterest));
        loan.setOutstandingBalance(Math.round(totalPayable));
        loan.setStartDate(LocalDate.now());
        loan.setClosed(false);

        Loan savedLoan = loanRepo.save(loan);
        generateEmiSchedule(savedLoan);

        return savedLoan;
    }

    // 2️⃣ GENERATE EMI SCHEDULE
    private void generateEmiSchedule(Loan loan) {

        double balance = loan.getPrincipal();
        double r = loan.getInterestRate() / 12 / 100;

        for (int i = 1; i <= loan.getTenureMonths(); i++) {

            double interest = balance * r;
            double principal = loan.getEmiAmount() - interest;
            balance -= principal;

            EmiSchedule emi = new EmiSchedule();
            emi.setLoanId(loan.getId());
            emi.setMonthNo(i);
            emi.setEmiAmount(loan.getEmiAmount());
            emi.setInterestComponent(Math.round(interest));
            emi.setPrincipalComponent(Math.round(principal));
            emi.setRemainingBalance(Math.max(Math.round(balance), 0));
            emi.setDueDate(loan.getStartDate().plusMonths(i));
            emi.setPaid(false);

            emiRepo.save(emi);
        }
    }

    // 3️⃣ GET EMI SCHEDULE
    public List<EmiSchedule> getSchedule(Long loanId) {
        return emiRepo.findByLoanIdOrderByMonthNo(loanId);
    }

    // 4️⃣ PAY EMI
    public void payEmi(Long emiId) {

        EmiSchedule emi = emiRepo.findById(emiId)
                .orElseThrow(() -> new RuntimeException("EMI not found"));

        if (emi.isPaid()) return;

        emi.setPaid(true);
        emiRepo.save(emi);

        Loan loan = loanRepo.findById(emi.getLoanId())
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        loan.setOutstandingBalance(emi.getRemainingBalance());

        if (loan.getOutstandingBalance() <= 0) {
            loan.setClosed(true);
        }

        loanRepo.save(loan);
    }
}
