package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.*;
import com.pro.finance.selffinanceapp.model.EmiSchedule;
import com.pro.finance.selffinanceapp.model.Loan;
import com.pro.finance.selffinanceapp.repository.EmiScheduleRepository;
import com.pro.finance.selffinanceapp.repository.LoanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final LoanRepository        loanRepo;
    private final EmiScheduleRepository emiRepo;
    private final EmiService            emiService;   // one-way dependency

    // ─────────────────────────────────────────────────────────────
    // 1. ADD LOAN + trigger EMI schedule generation
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public LoanResponseDTO addLoan(LoanRequestDTO request) {

        double r = request.getInterestRate() / 12.0 / 100.0;
        int    n = request.getTenureMonths();
        double p = request.getPrincipal();

        // Standard reducing-balance EMI formula
        double emi = (r == 0)
                ? p / n
                : (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);

        double emiRounded    = round2(emi);
        double totalPayable  = round2(emiRounded * n);
        double totalInterest = round2(totalPayable - p);

        Loan loan = Loan.builder()
                .userId(request.getUserId())
                .loanName(request.getLoanName())
                .loanType(request.getLoanType())
                .principal(p)
                .interestRate(request.getInterestRate())
                .tenureMonths(n)
                .emiAmount(emiRounded)
                .totalPayable(totalPayable)
                .totalInterest(totalInterest)
                .outstandingBalance(round2(p))   // outstanding = principal, not totalPayable
                .startDate(LocalDate.now())
                .closed(false)
                .build();

        Loan savedLoan = loanRepo.save(loan);

        // Delegate schedule generation to EmiService
        emiService.generateSchedule(savedLoan, r, emiRounded);

        return toResponseDTO(savedLoan);
    }

    // ─────────────────────────────────────────────────────────────
    // 2. GET ALL LOANS FOR A USER
    // ─────────────────────────────────────────────────────────────
    public List<LoanResponseDTO> getLoansByUser(Long userId) {
        return loanRepo.findByUserId(userId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────
    // 3. GET SINGLE LOAN
    // ─────────────────────────────────────────────────────────────
    public LoanResponseDTO getLoanById(Long loanId) {
        return toResponseDTO(findLoanOrThrow(loanId));
    }

    // ─────────────────────────────────────────────────────────────
    // 4. GET LOAN SUMMARY (dashboard + chart data)
    // ─────────────────────────────────────────────────────────────
    public LoanSummaryDTO getLoanSummary(Long loanId) {

        Loan loan = findLoanOrThrow(loanId);
        List<EmiSchedule> schedule = emiRepo.findByLoanIdOrderByMonthNo(loanId);

        int    n           = loan.getTenureMonths();
        int    paidMonths  = emiRepo.countByLoanIdAndPaidTrue(loanId);
        double progressPct = round2((paidMonths * 100.0) / n);

        double totalPrincipalPaid = emiRepo.getTotalPrincipalPaid(loanId);
        double totalInterestPaid  = emiRepo.getTotalInterestPaid(loanId);
        double totalAmountPaid    = emiRepo.getTotalPaid(loanId);

        // Build chart arrays (index = monthNo - 1)
        double[] principalArr = new double[n];
        double[] interestArr  = new double[n];
        double[] balanceArr   = new double[n];

        for (EmiSchedule e : schedule) {
            int idx = e.getMonthNo() - 1;
            principalArr[idx] = e.getPrincipalComponent();
            interestArr[idx]  = e.getInterestComponent();
            balanceArr[idx]   = e.getRemainingBalance();
        }

        return LoanSummaryDTO.builder()
                .loanId(loanId)
                .loanName(loan.getLoanName())
                .totalMonths(n)
                .paidMonths(paidMonths)
                .remainingMonths(n - paidMonths)
                .progressPercent(progressPct)
                .principal(loan.getPrincipal())
                .totalPayable(loan.getTotalPayable())
                .totalInterest(loan.getTotalInterest())
                .totalPrincipalPaid(round2(totalPrincipalPaid))
                .totalInterestPaid(round2(totalInterestPaid))
                .totalAmountPaid(round2(totalAmountPaid))
                .outstandingBalance(loan.getOutstandingBalance())
                .principalComponents(principalArr)
                .interestComponents(interestArr)
                .remainingBalances(balanceArr)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // 5. DELETE LOAN (cascades to EMI schedule)
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void deleteLoan(Long loanId) {
        findLoanOrThrow(loanId);
        emiRepo.deleteAll(emiRepo.findByLoanIdOrderByMonthNo(loanId));
        loanRepo.deleteById(loanId);
    }

    // ─────────────────────────────────────────────────────────────
    // PACKAGE-PRIVATE: called by EmiService after payEmi()
    // to update outstanding balance and close the loan if done
    // ─────────────────────────────────────────────────────────────
    @Transactional
    void updateOutstandingBalance(Long loanId, double remainingBalance) {
        Loan loan = findLoanOrThrow(loanId);
        loan.setOutstandingBalance(remainingBalance);
        if (remainingBalance <= 0) {
            loan.setClosed(true);
        }
        loanRepo.save(loan);
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────
    Loan findLoanOrThrow(Long loanId) {
        return loanRepo.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found: " + loanId));
    }

    private LoanResponseDTO toResponseDTO(Loan loan) {
        return LoanResponseDTO.builder()
                .id(loan.getId())
                .userId(loan.getUserId())
                .loanName(loan.getLoanName())
                .loanType(loan.getLoanType())
                .principal(loan.getPrincipal())
                .interestRate(loan.getInterestRate())
                .tenureMonths(loan.getTenureMonths())
                .emiAmount(loan.getEmiAmount())
                .totalInterest(loan.getTotalInterest())
                .totalPayable(loan.getTotalPayable())
                .outstandingBalance(loan.getOutstandingBalance())
                .startDate(loan.getStartDate())
                .closed(loan.isClosed())
                .build();
    }

    private double round2(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}