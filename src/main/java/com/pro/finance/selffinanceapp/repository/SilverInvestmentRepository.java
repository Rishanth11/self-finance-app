package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.SilverInvestment;
import com.pro.finance.selffinanceapp.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SilverInvestmentRepository extends JpaRepository<SilverInvestment, Long> {

    List<SilverInvestment> findByUser(User user);
}