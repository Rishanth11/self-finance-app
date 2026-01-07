package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.DigitalGold;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DigitalGoldRepository
        extends JpaRepository<DigitalGold, Long> {

    List<DigitalGold> findByUserId(Long userId);
}

