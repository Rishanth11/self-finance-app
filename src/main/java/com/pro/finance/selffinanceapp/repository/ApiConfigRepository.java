package com.pro.finance.selffinanceapp.repository;

import com.pro.finance.selffinanceapp.model.ApiConfigEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApiConfigRepository extends JpaRepository<ApiConfigEntity, Long> {

    Optional<ApiConfigEntity> findByConfigKey(String configKey);

    List<ApiConfigEntity> findByCategory(String category);

    boolean existsByConfigKey(String configKey);
}