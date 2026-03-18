package com.pro.finance.selffinanceapp.service;

import com.pro.finance.selffinanceapp.dto.BillRequestDTO;
import com.pro.finance.selffinanceapp.dto.BillReminderDTO;
import com.pro.finance.selffinanceapp.dto.BillResponseDTO;
import com.pro.finance.selffinanceapp.model.Bill;
import com.pro.finance.selffinanceapp.model.BillCategory;
import com.pro.finance.selffinanceapp.model.BillFrequency;
import com.pro.finance.selffinanceapp.repository.BillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BillService {

    private final BillRepository billRepo;

    // ─────────────────────────────────────────────────────────────
    // 1. ADD BILL
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public BillResponseDTO addBill(BillRequestDTO request) {
        Bill bill = Bill.builder()
                .userId(request.getUserId())
                .billName(request.getBillName())
                .category(request.getCategory())
                .frequency(request.getFrequency())
                .amount(request.getAmount())
                .notes(request.getNotes())
                .startDate(request.getStartDate())
                .nextDueDate(request.getStartDate())
                .paid(false)
                .build();
        return toDTO(billRepo.save(bill));
    }

    // ─────────────────────────────────────────────────────────────
    // 2. GET ALL BILLS FOR USER
    // ─────────────────────────────────────────────────────────────
    public List<BillResponseDTO> getBillsByUser(Long userId) {
        return billRepo.findByUserIdOrderByNextDueDateAsc(userId)
                .stream().map(this::toDTO).toList();
    }

    // ─────────────────────────────────────────────────────────────
    // 3. GET SINGLE BILL
    // ─────────────────────────────────────────────────────────────
    public BillResponseDTO getBillById(Long billId) {
        return toDTO(findOrThrow(billId));
    }

    // ─────────────────────────────────────────────────────────────
    // 4. UPDATE BILL
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public BillResponseDTO updateBill(Long billId, BillRequestDTO request) {
        Bill bill = findOrThrow(billId);
        bill.setBillName(request.getBillName());
        bill.setCategory(request.getCategory());
        bill.setFrequency(request.getFrequency());
        bill.setAmount(request.getAmount());
        bill.setNotes(request.getNotes());
        return toDTO(billRepo.save(bill));
    }

    // ─────────────────────────────────────────────────────────────
    // 5. MARK AS PAID (current cycle)
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public BillResponseDTO markAsPaid(Long billId) {
        Bill bill = findOrThrow(billId);
        if (bill.isPaid()) throw new RuntimeException("Bill is already marked as paid.");
        bill.setPaid(true);
        bill.setPaidDate(LocalDate.now());
        return toDTO(billRepo.save(bill));
    }

    // ─────────────────────────────────────────────────────────────
    // 6. RESET FOR NEXT CYCLE (manually advance due date)
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public BillResponseDTO resetForNextCycle(Long billId) {
        Bill bill = findOrThrow(billId);
        bill.setNextDueDate(computeNextDueDate(bill.getNextDueDate(), bill.getFrequency()));
        bill.setPaid(false);
        bill.setPaidDate(null);
        return toDTO(billRepo.save(bill));
    }

    // ─────────────────────────────────────────────────────────────
    // 7. DELETE BILL
    // ─────────────────────────────────────────────────────────────
    @Transactional
    public void deleteBill(Long billId) {
        findOrThrow(billId);
        billRepo.deleteById(billId);
    }

    // ─────────────────────────────────────────────────────────────
    // 8. GET REMINDERS (overdue + due today + due within 7 days)
    // ─────────────────────────────────────────────────────────────
    public BillReminderDTO getReminders(Long userId) {
        LocalDate today   = LocalDate.now();
        LocalDate in7Days = today.plusDays(7);

        List<BillResponseDTO> overdueList  = billRepo.findOverdue(userId, today)
                .stream().map(this::toDTO).toList();
        List<BillResponseDTO> dueTodayList = billRepo.findDueBetween(userId, today, today)
                .stream().map(this::toDTO).toList();
        List<BillResponseDTO> dueSoonList  = billRepo.findDueBetween(userId, today.plusDays(1), in7Days)
                .stream().map(this::toDTO).toList();

        double totalOverdue  = overdueList.stream().mapToDouble(BillResponseDTO::getAmount).sum();
        double totalDueSoon  = dueTodayList.stream().mapToDouble(BillResponseDTO::getAmount).sum()
                + dueSoonList.stream().mapToDouble(BillResponseDTO::getAmount).sum();

        return BillReminderDTO.builder()
                .overdueCount(overdueList.size())
                .dueTodayCount(dueTodayList.size())
                .dueSoonCount(dueSoonList.size())
                .totalOverdueAmount(totalOverdue)
                .totalDueSoonAmount(totalDueSoon)
                .overdueList(overdueList)
                .dueTodayList(dueTodayList)
                .dueSoonList(dueSoonList)
                .build();
    }

    // ─────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────
    private Bill findOrThrow(Long billId) {
        return billRepo.findById(billId)
                .orElseThrow(() -> new RuntimeException("Bill not found: " + billId));
    }

    public LocalDate computeNextDueDate(LocalDate current, BillFrequency frequency) {
        return switch (frequency) {
            case MONTHLY   -> current.plusMonths(1);
            case QUARTERLY -> current.plusMonths(3);
            case ANNUAL    -> current.plusYears(1);
        };
    }

    private String resolveStatus(Bill bill) {
        if (bill.isPaid()) return "PAID";
        LocalDate today = LocalDate.now();
        LocalDate due   = bill.getNextDueDate();
        if (due.isBefore(today))                    return "OVERDUE";
        if (due.isEqual(today))                     return "DUE_TODAY";
        if (!due.isAfter(today.plusDays(7)))         return "DUE_SOON";
        return "UPCOMING";
    }

    private BillResponseDTO toDTO(Bill bill) {
        long daysUntil = ChronoUnit.DAYS.between(LocalDate.now(), bill.getNextDueDate());
        return BillResponseDTO.builder()
                .id(bill.getId())
                .userId(bill.getUserId())
                .billName(bill.getBillName())
                .category(bill.getCategory())
                .categoryLabel(categoryLabel(bill.getCategory()))
                .notes(bill.getNotes())
                .frequency(bill.getFrequency())
                .frequencyLabel(frequencyLabel(bill.getFrequency()))
                .amount(bill.getAmount())
                .startDate(bill.getStartDate())
                .nextDueDate(bill.getNextDueDate())
                .paid(bill.isPaid())
                .paidDate(bill.getPaidDate())
                .status(resolveStatus(bill))
                .daysUntilDue(daysUntil)
                .build();
    }

    private String categoryLabel(BillCategory c) {
        return switch (c) {
            case UTILITIES      -> "Utilities";
            case SUBSCRIPTION   -> "Subscription";
            case INSURANCE      -> "Insurance";
            case RENT           -> "Rent";
            case PHONE_INTERNET -> "Phone / Internet";
            case OTHER          -> "Other";
        };
    }

    private String frequencyLabel(BillFrequency f) {
        return switch (f) {
            case MONTHLY   -> "Monthly";
            case QUARTERLY -> "Quarterly";
            case ANNUAL    -> "Annual";
        };
    }
}