package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.EmiScheduleDTO;
import com.pro.finance.selffinanceapp.model.EmiSchedule;
import com.pro.finance.selffinanceapp.model.Loan;
import com.pro.finance.selffinanceapp.repository.EmiScheduleRepository;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class EmiService {

    private final EmiScheduleRepository emiRepo;
    private final LoanService loanService;

    // @Lazy must be on the constructor parameter — NOT on the field.
    // @RequiredArgsConstructor cannot carry @Lazy, so we use an explicit constructor.
    // LoanService → EmiService  : eager (normal injection)
    // EmiService  → LoanService : lazy (Spring injects a proxy, resolved on first call)
    public EmiService(EmiScheduleRepository emiRepo, @Lazy LoanService loanService) {
        this.emiRepo     = emiRepo;
        this.loanService = loanService;
    }

    // ─────────────────────────────────────────────────────────────
    // 1. GENERATE EMI SCHEDULE
    //    Called by LoanService right after saving the Loan entity
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void generateSchedule(Loan loan, double monthlyRate, double emi) {

        double balance = loan.getPrincipal();
        int    n       = loan.getTenureMonths();

        for (int i = 1; i <= n; i++) {

            double interest  = round2(balance * monthlyRate);
            double principal;

            if (i == n) {
                // Last EMI: clear exact remaining balance (fixes floating-point drift)
                principal = round2(balance);
            } else {
                principal = round2(emi - interest);
            }

            balance = round2(balance - principal);

            EmiSchedule entry = EmiSchedule.builder()
                    .loanId(loan.getId())
                    .monthNo(i)
                    .emiAmount(i == n ? round2(interest + principal) : emi)
                    .interestComponent(interest)
                    .principalComponent(principal)
                    .remainingBalance(Math.max(balance, 0))
                    .dueDate(loan.getStartDate().plusMonths(i))
                    .paid(false)
                    .build();

            emiRepo.save(entry);
        }
    }

    // ─────────────────────────────────────────────────────────────
    // 2. GET FULL EMI SCHEDULE
    // ─────────────────────────────────────────────────────────────
    public List<EmiScheduleDTO> getSchedule(Long loanId) {
        return emiRepo.findByLoanIdOrderByMonthNo(loanId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────
    // 3. GET UPCOMING (unpaid) EMIs
    // ─────────────────────────────────────────────────────────────
    public List<EmiScheduleDTO> getUpcomingEmis(Long loanId) {
        return emiRepo.findByLoanIdAndPaidFalseOrderByMonthNoAsc(loanId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────
    // 4. PAY EMI (sequential enforcement)
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public EmiScheduleDTO payEmi(Long emiId) {

        EmiSchedule emi = emiRepo.findById(emiId)
                .orElseThrow(() -> new RuntimeException("EMI not found: " + emiId));

        if (emi.isPaid()) {
            throw new RuntimeException("EMI #" + emi.getMonthNo() + " is already paid");
        }

        // Enforce sequential payment: the requested EMI must be the first unpaid one
        EmiSchedule firstUnpaid = emiRepo
                .findFirstByLoanIdAndPaidFalseOrderByMonthNoAsc(emi.getLoanId())
                .orElseThrow(() -> new RuntimeException("No unpaid EMIs found for this loan"));

        if (!firstUnpaid.getId().equals(emiId)) {
            throw new RuntimeException(
                    "Please pay EMI #" + firstUnpaid.getMonthNo() + " first (sequential payment required)"
            );
        }

        emi.setPaid(true);
        emi.setPaidDate(LocalDate.now());
        emiRepo.save(emi);

        // Delegate loan balance update back to LoanService
        loanService.updateOutstandingBalance(emi.getLoanId(), emi.getRemainingBalance());

        return toDTO(emi);
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────
    private EmiScheduleDTO toDTO(EmiSchedule e) {
        return EmiScheduleDTO.builder()
                .id(e.getId())
                .loanId(e.getLoanId())
                .monthNo(e.getMonthNo())
                .emiAmount(e.getEmiAmount())
                .principalComponent(e.getPrincipalComponent())
                .interestComponent(e.getInterestComponent())
                .remainingBalance(e.getRemainingBalance())
                .dueDate(e.getDueDate())
                .paid(e.isPaid())
                .paidDate(e.getPaidDate())
                .status(resolveStatus(e))
                .build();
    }

    private String resolveStatus(EmiSchedule e) {
        if (e.isPaid()) return "PAID";
        LocalDate today = LocalDate.now();
        if (e.getDueDate().isBefore(today)) return "OVERDUE";
        if (e.getDueDate().isEqual(today))  return "DUE_TODAY";
        return "UPCOMING";
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}