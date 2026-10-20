package com.coderaah.medtrack.identity.repository;

import com.coderaah.medtrack.identity.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByPersonId(Long personId);
}