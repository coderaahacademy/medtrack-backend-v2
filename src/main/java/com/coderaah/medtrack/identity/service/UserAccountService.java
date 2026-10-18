package com.coderaah.medtrack.identity.service;

import com.coderaah.medtrack.identity.domain.Person;
import com.coderaah.medtrack.identity.domain.Role;
import com.coderaah.medtrack.identity.domain.UserAccount;
import com.coderaah.medtrack.identity.domain.UserAccountStatus;
import com.coderaah.medtrack.identity.domain.UserRole;
import com.coderaah.medtrack.identity.dto.AssignRoleRequest;
import com.coderaah.medtrack.identity.dto.ChangeUserAccountStatusRequest;
import com.coderaah.medtrack.identity.dto.CreateUserAccountRequest;
import com.coderaah.medtrack.identity.dto.UserAccountResponse;
import com.coderaah.medtrack.identity.exception.UserAccountNotFoundException;
import com.coderaah.medtrack.identity.repository.PersonRepository;
import com.coderaah.medtrack.identity.repository.UserAccountRepository;
import com.coderaah.medtrack.identity.repository.UserRoleRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserAccountService {

    private final UserAccountRepository userAccountRepository;
    private final UserRoleRepository userRoleRepository;
    private final PersonRepository personRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserAccountService(
            UserAccountRepository userAccountRepository,
            UserRoleRepository userRoleRepository,
            PersonRepository personRepository
    ) {
        this.userAccountRepository = userAccountRepository;
        this.userRoleRepository = userRoleRepository;
        this.personRepository = personRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public UserAccountResponse createUserAccount(
            CreateUserAccountRequest request
    ) {
        String email = request.getEmail().trim().toLowerCase();

        if (userAccountRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                    "Email is already in use"
            );
        }

        Person person;

        if (request.getPersonId() != null) {

            person = personRepository.findById(request.getPersonId())
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "Person not found: "
                                            + request.getPersonId()
                            )
                    );

            if (userAccountRepository.existsByPersonId(person.getId())) {
                throw new IllegalArgumentException(
                        "This person already has a user account"
                );
            }

        } else {

            if (request.getFirstName() == null
                    || request.getFirstName().isBlank()) {

                throw new IllegalArgumentException(
                        "First name is required"
                );
            }

            if (request.getLastName() == null
                    || request.getLastName().isBlank()) {

                throw new IllegalArgumentException(
                        "Last name is required"
                );
            }

            person = new Person(
                    request.getFirstName().trim(),
                    request.getLastName().trim(),
                    request.getBirthDate(),
                    request.getPhone()
            );

            person = personRepository.save(person);
        }

        String passwordHash =
                passwordEncoder.encode(request.getPassword());

        UserAccount userAccount = new UserAccount(
                person,
                email,
                passwordHash,
                UserAccountStatus.PENDING
        );

        userAccount = userAccountRepository.save(userAccount);

        return toResponse(userAccount);
    }

    @Transactional(readOnly = true)
    public UserAccountResponse getUserAccountById(Long id) {

        UserAccount userAccount =
                userAccountRepository.findById(id)
                        .orElseThrow(() ->
                                new UserAccountNotFoundException(
                                        "User account not found: " + id
                                )
                        );

        return toResponse(userAccount);
    }

    @Transactional(readOnly = true)
    public UserAccountResponse getUserAccountByEmail(
            String email
    ) {

        UserAccount userAccount =
                userAccountRepository.findByEmail(
                                email.trim().toLowerCase()
                        )
                        .orElseThrow(() ->
                                new UserAccountNotFoundException(
                                        "User account not found for email: "
                                                + email
                                )
                        );

        return toResponse(userAccount);
    }

    @Transactional(readOnly = true)
    public List<UserAccountResponse> getAllUserAccounts() {

        return userAccountRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public UserAccountResponse changeStatus(
            Long id,
            ChangeUserAccountStatusRequest request
    ) {

        UserAccount userAccount = getEntityById(id);

        userAccount.setStatus(request.getStatus());

        userAccount = userAccountRepository.save(userAccount);

        return toResponse(userAccount);
    }

    @Transactional(readOnly = true)
    public List<Role> getRoles(Long userAccountId) {

        getEntityById(userAccountId);

        return userRoleRepository
                .findByUserAccountIdOrderByRoleAsc(userAccountId)
                .stream()
                .map(UserRole::getRole)
                .toList();
    }

    public List<Role> assignRole(
            Long userAccountId,
            AssignRoleRequest request
    ) {

        UserAccount userAccount = getEntityById(userAccountId);

        Role role = request.getRole();

        if (userRoleRepository.existsByUserAccountIdAndRole(
                userAccountId,
                role
        )) {
            throw new IllegalArgumentException(
                    "Role is already assigned to this user"
            );
        }

        UserRole userRole = new UserRole(
                userAccount,
                role
        );

        userRoleRepository.save(userRole);

        return getRoles(userAccountId);
    }

    public List<Role> removeRole(
            Long userAccountId,
            Role role
    ) {

        getEntityById(userAccountId);

        if (!userRoleRepository.existsByUserAccountIdAndRole(
                userAccountId,
                role
        )) {
            throw new IllegalArgumentException(
                    "Role is not assigned to this user"
            );
        }

        userRoleRepository.deleteByUserAccountIdAndRole(
                userAccountId,
                role
        );

        return getRoles(userAccountId);
    }

    private UserAccount getEntityById(Long id) {

        return userAccountRepository.findById(id)
                .orElseThrow(() ->
                        new UserAccountNotFoundException(
                                "User account not found: " + id
                        )
                );
    }

    private UserAccountResponse toResponse(
            UserAccount userAccount
    ) {

        Person person = userAccount.getPerson();

        UserAccountResponse response =
                new UserAccountResponse();
        response.setId(userAccount.getId());
        response.setPersonId(person.getId());
        response.setFirstName(person.getFirstName());
        response.setLastName(person.getLastName());
        response.setBirthDate(person.getBirthDate());
        response.setPhone(person.getPhone());

        response.setEmail(userAccount.getEmail());
        response.setStatus(userAccount.getStatus());
        response.setLastLoginAt(userAccount.getLastLoginAt());
        response.setCreatedAt(userAccount.getCreatedAt());
        response.setUpdatedAt(userAccount.getUpdatedAt());

        return response;
    }
}