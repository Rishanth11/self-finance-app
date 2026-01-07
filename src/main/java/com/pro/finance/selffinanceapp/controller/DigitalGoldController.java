package com.pro.finance.selffinanceapp.controller;

import com.pro.finance.selffinanceapp.dto.DigitalGoldDTO;
import com.pro.finance.selffinanceapp.model.DigitalGold;
import com.pro.finance.selffinanceapp.model.User;
import com.pro.finance.selffinanceapp.service.DigitalGoldService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gold")
public class DigitalGoldController {

    private final DigitalGoldService service;

    public DigitalGoldController(DigitalGoldService service) {
        this.service = service;
    }

    // ➕ Add Gold
    @PostMapping
    public DigitalGold addGold(
            @RequestBody DigitalGoldDTO dto,
            @AuthenticationPrincipal User user) {

        return service.addGold(dto, user);
    }

    // 🔄 Update Live Gold Value
    @GetMapping("/update")
    public List<DigitalGold> updateGold(
            @AuthenticationPrincipal User user) {

        return service.updateGoldValue(user.getId());
    }
}
