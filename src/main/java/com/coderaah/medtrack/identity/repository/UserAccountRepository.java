package com.coderaah.medtrack.identity.repository;

import com.coderaah.medtrack.identity.domain.UserAccount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

}