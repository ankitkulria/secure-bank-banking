package com.training.repository;

import com.training.entity.Account;
import com.training.entity.AccountType;
import com.training.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account,Long> {

    List<Account> findByUserId(Long userId);

    Optional<Account> findByAccountNumber(String accountNumber);

    boolean existsByUserAndAccountType(User user, AccountType accountType);

}
