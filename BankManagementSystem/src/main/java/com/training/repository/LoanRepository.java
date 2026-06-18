package com.training.repository;

import com.training.entity.Loan;
import com.training.entity.LoanStatus;
import com.training.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LoanRepository
        extends JpaRepository<Loan, Long> {

    List<Loan> findByUser(User user);

    List<Loan> findByStatus(LoanStatus status);

    Optional<Loan> findByUserAndStatus(User user, LoanStatus status);

    Optional<Loan> findTopByUserOrderByAppliedAtDesc(User user);

    boolean existsByUserAndStatus(User user, LoanStatus status);

    long countByStatus(LoanStatus status);
}