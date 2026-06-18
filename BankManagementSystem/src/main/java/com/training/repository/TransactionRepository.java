package com.training.repository;

import com.training.entity.Transaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction,Long> {

    List<Transaction> findBySourceAccountIdOrderByTimeStampDesc(Long accountId);

    Page<Transaction> findBySourceAccountId(Long accountId, Pageable pageable);
}
