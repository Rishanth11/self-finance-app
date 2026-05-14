package com.rishanth.flux360.repository;

import com.rishanth.flux360.model.SilverInvestment;
import com.rishanth.flux360.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SilverInvestmentRepository extends JpaRepository<SilverInvestment, Long> {

    List<SilverInvestment> findByUser(User user);
}