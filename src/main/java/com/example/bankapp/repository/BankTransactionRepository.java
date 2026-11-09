package com.example.bankapp.repository;

import com.example.bankapp.model.BankTransaction;
import com.example.bankapp.model.TransactionStatus;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, Long> {

        @Query("SELECT t FROM BankTransaction t ORDER BY t.createdAt DESC")
        List<BankTransaction> findAllLatest();

        @Query("SELECT t FROM BankTransaction t " +
                        "WHERE t.fromAccount = :accountNumber OR t.toAccount = :accountNumber " +
                        "ORDER BY t.createdAt DESC")
        List<BankTransaction> findHistory(@Param("accountNumber") String accountNumber);

        @Query("SELECT t FROM BankTransaction t WHERE t.status = :status ORDER BY t.createdAt DESC")
        List<BankTransaction> findByStatus(@Param("status") TransactionStatus status);

        @Query("SELECT COUNT(t) FROM BankTransaction t WHERE t.status = :status")
        long countByStatus(@Param("status") TransactionStatus status);
}