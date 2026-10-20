package com.coderaah.medtrack.identity.repository;

import com.coderaah.medtrack.identity.domain.Role;
import com.coderaah.medtrack.identity.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRoleRepository extends JpaRepository<UserRole, Long> {

    List<UserRole> findByUserAccountIdOrderByRoleAsc(Long userAccountId);

    boolean existsByUserAccountIdAndRole(Long userAccountId, Role role);

    Optional<UserRole> findByUserAccountIdAndRole(Long userAccountId, Role role);

    void deleteByUserAccountIdAndRole(Long userAccountId, Role role);
}