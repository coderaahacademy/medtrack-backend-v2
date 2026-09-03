package com.coderaah.medtrack.identity.repository;

import com.coderaah.medtrack.identity.domain.Person;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<Person,Long> {
}
