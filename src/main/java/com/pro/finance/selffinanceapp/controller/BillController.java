package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.dto.BillReminderDTO;
import com.pro.finance.selffinanceapp.dto.BillRequestDTO;
import com.pro.finance.selffinanceapp.dto.BillResponseDTO;
import com.pro.finance.selffinanceapp.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bills")
@RequiredArgsConstructor
public class BillController {

    private final BillService billService;

    // ADD BILL
    @PostMapping
    public ResponseEntity<BillResponseDTO> addBill(@Valid @RequestBody BillRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(billService.addBill(request));
    }

    // GET ALL BILLS FOR USER
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<BillResponseDTO>> getBills(@PathVariable Long userId) {
        return ResponseEntity.ok(billService.getBillsByUser(userId));
    }

    // GET SINGLE BILL
    @GetMapping("/{billId}")
    public ResponseEntity<BillResponseDTO> getBill(@PathVariable Long billId) {
        return ResponseEntity.ok(billService.getBillById(billId));
    }

    // UPDATE BILL
    @PutMapping("/{billId}")
    public ResponseEntity<BillResponseDTO> updateBill(@PathVariable Long billId,
                                                      @Valid @RequestBody BillRequestDTO request) {
        return ResponseEntity.ok(billService.updateBill(billId, request));
    }

    // MARK AS PAID
    @PutMapping("/{billId}/pay")
    public ResponseEntity<BillResponseDTO> markAsPaid(@PathVariable Long billId) {
        return ResponseEntity.ok(billService.markAsPaid(billId));
    }

    // RESET FOR NEXT CYCLE
    @PutMapping("/{billId}/reset")
    public ResponseEntity<BillResponseDTO> resetForNextCycle(@PathVariable Long billId) {
        return ResponseEntity.ok(billService.resetForNextCycle(billId));
    }

    // GET REMINDERS
    @GetMapping("/reminders/{userId}")
    public ResponseEntity<BillReminderDTO> getReminders(@PathVariable Long userId) {
        return ResponseEntity.ok(billService.getReminders(userId));
    }

    // DELETE BILL
    @DeleteMapping("/{billId}")
    public ResponseEntity<String> deleteBill(@PathVariable Long billId) {
        billService.deleteBill(billId);
        return ResponseEntity.ok("Bill deleted successfully");
    }
}