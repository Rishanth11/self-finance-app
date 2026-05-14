package com.rishanth.flux360.repository;

import com.rishanth.flux360.model.ApiConfigEntity;
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