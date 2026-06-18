package com.training.repository;

import com.training.entity.OtpPurpose;
import com.training.entity.PendingUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PendingUserRepository
        extends JpaRepository<PendingUser, Long> {

    Optional<PendingUser> findByEmail(String email);

    Optional<PendingUser> findByEmailAndPurpose(String email, OtpPurpose purpose);

    void deleteByEmail(String email);
}
