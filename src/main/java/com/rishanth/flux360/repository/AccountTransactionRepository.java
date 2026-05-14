package com.rishanth.flux360.repository;

import com.rishanth.flux360.model.Account;
import com.rishanth.flux360.model.AccountTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AccountTransactionRepository extends JpaRepository<AccountTransaction, Long> {

    List<AccountTransaction> findByAccountOrderByDateDesc(Account account);

    List<AccountTransaction> findByAccountAndDateBetweenOrderByDateDesc(
            Account account, LocalDate start, LocalDate end);

    List<AccountTransaction> findByTransferPairId(Long transferPairId);
}