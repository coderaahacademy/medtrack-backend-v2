package com.coderaah.medtrack.identity.service;

import com.coderaah.medtrack.identity.domain.*;
import com.coderaah.medtrack.identity.dto.AssignRoleRequest;
import com.coderaah.medtrack.identity.dto.ChangeUserAccountStatusRequest;
import com.coderaah.medtrack.identity.dto.CreateUserAccountRequest;
import com.coderaah.medtrack.identity.dto.UserAccountResponse;
import com.coderaah.medtrack.identity.exception.DuplicateUserAccountException;
import com.coderaah.medtrack.identity.exception.InvalidUserAccountRequestException;
import com.coderaah.medtrack.identity.exception.UserAccountNotFoundException;
import com.coderaah.medtrack.identity.exception.UserRoleAlreadyAssignedException;
import com.coderaah.medtrack.identity.exception.UserRoleNotAssignedException;
import com.coderaah.medtrack.identity.repository.PersonRepository;
import com.coderaah.medtrack.identity.repository.UserAccountRepository;
import com.coderaah.medtrack.identity.repository.UserRoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    @Mock
    private UserAccountRepository userAccountRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private PersonRepository personRepository;

    private UserAccountService userAccountService;

    @BeforeEach
    void setUp() {
        userAccountService = new UserAccountService(
                userAccountRepository,
                userRoleRepository,
                personRepository
        );
    }

    @Test
    void createUserAccount_shouldCreateAccountWithHashedPassword() {

        CreateUserAccountRequest request = new CreateUserAccountRequest();
        request.setEmail("Test@Example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setBirthDate(LocalDate.of(1990, 1, 1));
        request.setPhone("123456789");

        Person person = new Person(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123456789"
        );
        person.setId(1L);

        when(userAccountRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        when(personRepository.save(any(Person.class)))
                .thenReturn(person);

        when(userAccountRepository.save(any(UserAccount.class)))
                .thenAnswer(invocation -> {
                    UserAccount account = invocation.getArgument(0);
                    account.setId(10L);
                    return account;
                });

        UserAccountResponse response =
                userAccountService.createUserAccount(request);

        assertNotNull(response);
        assertEquals(10L, response.getId());
        assertEquals(1L, response.getPersonId());
        assertEquals("test@example.com", response.getEmail());
        assertEquals(UserAccountStatus.PENDING, response.getStatus());

        ArgumentCaptor<UserAccount> captor =
                ArgumentCaptor.forClass(UserAccount.class);

        verify(userAccountRepository).save(captor.capture());

        UserAccount savedAccount = captor.getValue();

        assertNotEquals(
                "password123",
                savedAccount.getPasswordHash()
        );

        assertTrue(
                savedAccount.getPasswordHash() != null
                        && savedAccount.getPasswordHash().startsWith("$2")
        );
    }

    @Test
    void createUserAccount_shouldRejectDuplicateEmail() {

        CreateUserAccountRequest request = new CreateUserAccountRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFirstName("John");
        request.setLastName("Doe");

        when(userAccountRepository.existsByEmail("test@example.com"))
                .thenReturn(true);

        assertThrows(
                DuplicateUserAccountException.class,
                () -> userAccountService.createUserAccount(request)
        );

        verify(userAccountRepository, never())
                .save(any(UserAccount.class));
    }

    @Test
    void createUserAccount_shouldRejectPersonWithExistingAccount() {

        CreateUserAccountRequest request = new CreateUserAccountRequest();
        request.setPersonId(1L);
        request.setEmail("test@example.com");
        request.setPassword("password123");

        Person person = new Person(
                "John",
                "Doe",
                null,
                "123456789"
        );
        person.setId(1L);

        when(userAccountRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        when(personRepository.findById(1L))
                .thenReturn(Optional.of(person));

        when(userAccountRepository.existsByPersonId(1L))
                .thenReturn(true);

        assertThrows(
                DuplicateUserAccountException.class,
                () -> userAccountService.createUserAccount(request)
        );

        verify(userAccountRepository, never())
                .save(any(UserAccount.class));
    }

    @Test
    void createUserAccount_shouldRejectMissingFirstName() {

        CreateUserAccountRequest request = new CreateUserAccountRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setLastName("Doe");

        when(userAccountRepository.existsByEmail("test@example.com"))
                .thenReturn(false);

        assertThrows(
                InvalidUserAccountRequestException.class,
                () -> userAccountService.createUserAccount(request)
        );

        verify(personRepository, never())
                .save(any(Person.class));
    }

    @Test
    void getUserAccountById_shouldReturnAccount() {

        Person person = new Person(
                "John",
                "Doe",
                LocalDate.of(1990, 1, 1),
                "123456789"
        );
        person.setId(1L);

        UserAccount account = new UserAccount(
                person,
                "john@example.com",
                "hashed-password",
                UserAccountStatus.ACTIVE
        );
        account.setId(10L);

        when(userAccountRepository.findById(10L))
                .thenReturn(Optional.of(account));

        UserAccountResponse response =
                userAccountService.getUserAccountById(10L);

        assertEquals(10L, response.getId());
        assertEquals(1L, response.getPersonId());
        assertEquals("john@example.com", response.getEmail());
        assertEquals(UserAccountStatus.ACTIVE, response.getStatus());
    }

    @Test
    void getUserAccountById_shouldThrowWhenAccountDoesNotExist() {

        when(userAccountRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                UserAccountNotFoundException.class,
                () -> userAccountService.getUserAccountById(999L)
        );
    }

    @Test
    void getUserAccountByEmail_shouldNormalizeEmail() {

        Person person = new Person(
                "John",
                "Doe",
                null,
                null
        );
        person.setId(1L);

        UserAccount account = new UserAccount(
                person,
                "john@example.com",
                "hashed-password",
                UserAccountStatus.ACTIVE
        );
        account.setId(10L);

        when(userAccountRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(account));

        UserAccountResponse response =
                userAccountService.getUserAccountByEmail(
                        "  JOHN@EXAMPLE.COM "
                );

        assertEquals("john@example.com", response.getEmail());

        verify(userAccountRepository)
                .findByEmail("john@example.com");
    }

    @Test
    void getAllUserAccounts_shouldReturnAllAccounts() {

        Person person1 = new Person(
                "John",
                "Doe",
                null,
                null
        );
        person1.setId(1L);

        Person person2 = new Person(
                "Jane",
                "Smith",
                null,
                null
        );
        person2.setId(2L);

        UserAccount account1 = new UserAccount(
                person1,
                "john@example.com",
                "hash1",
                UserAccountStatus.ACTIVE
        );
        account1.setId(1L);

        UserAccount account2 = new UserAccount(
                person2,
                "jane@example.com",
                "hash2",
                UserAccountStatus.PENDING
        );
        account2.setId(2L);

        when(userAccountRepository.findAll())
                .thenReturn(List.of(account1, account2));

        List<UserAccountResponse> responses =
                userAccountService.getAllUserAccounts();

        assertEquals(2, responses.size());
        assertEquals("john@example.com", responses.get(0).getEmail());
        assertEquals("jane@example.com", responses.get(1).getEmail());
    }

    @Test
    void changeStatus_shouldUpdateAccountStatus() {

        Person person = new Person(
                "John",
                "Doe",
                null,
                null
        );
        person.setId(1L);

        UserAccount account = new UserAccount(
                person,
                "john@example.com",
                "hashed-password",
                UserAccountStatus.PENDING
        );
        account.setId(10L);

        ChangeUserAccountStatusRequest request =
                new ChangeUserAccountStatusRequest();

        request.setStatus(UserAccountStatus.ACTIVE);

        when(userAccountRepository.findById(10L))
                .thenReturn(Optional.of(account));

        when(userAccountRepository.save(account))
                .thenReturn(account);

        UserAccountResponse response =
                userAccountService.changeStatus(10L, request);

        assertEquals(
                UserAccountStatus.ACTIVE,
                response.getStatus()
        );

        verify(userAccountRepository).save(account);
    }

    @Test
    void assignRole_shouldAssignRole() {

        Person person = new Person(
                "John",
                "Doe",
                null,
                null
        );
        person.setId(1L);

        UserAccount account = new UserAccount(
                person,
                "john@example.com",
                "hashed-password",
                UserAccountStatus.ACTIVE
        );
        account.setId(10L);

        AssignRoleRequest request =
                new AssignRoleRequest();

        request.setRole(Role.DOCTOR);

        when(userAccountRepository.findById(10L))
                .thenReturn(Optional.of(account));

        when(userRoleRepository.existsByUserAccountIdAndRole(
                10L,
                Role.DOCTOR
        )).thenReturn(false);

        when(userRoleRepository.findByUserAccountIdOrderByRoleAsc(10L))
                .thenReturn(List.of(
                        new UserRole(account, Role.DOCTOR)
                ));

        List<Role> roles =
                userAccountService.assignRole(10L, request);

        assertEquals(1, roles.size());
        assertEquals(Role.DOCTOR, roles.get(0));

        verify(userRoleRepository)
                .save(any(UserRole.class));
    }

    @Test
    void assignRole_shouldRejectDuplicateRole() {

        Person person = new Person(
                "John",
                "Doe",
                null,
                null
        );
        person.setId(1L);

        UserAccount account = new UserAccount(
                person,
                "john@example.com",
                "hashed-password",
                UserAccountStatus.ACTIVE
        );
        account.setId(10L);

        AssignRoleRequest request =
                new AssignRoleRequest();

        request.setRole(Role.DOCTOR);

        when(userAccountRepository.findById(10L))
                .thenReturn(Optional.of(account));

        when(userRoleRepository.existsByUserAccountIdAndRole(
                10L,
                Role.DOCTOR
        )).thenReturn(true);

        assertThrows(
                UserRoleAlreadyAssignedException.class,
                () -> userAccountService.assignRole(10L, request)
        );

        verify(userRoleRepository, never())
                .save(any(UserRole.class));
    }

    @Test
    void getRoles_shouldReturnAssignedRoles() {

        Person person = new Person(
                "John",
                "Doe",
                null,
                null
        );
        person.setId(1L);

        UserAccount account = new UserAccount(
                person,
                "john@example.com",
                "hashed-password",
                UserAccountStatus.ACTIVE
        );
        account.setId(10L);

        UserRole doctorRole =
                new UserRole(account, Role.DOCTOR);

        UserRole patientRole =
                new UserRole(account, Role.PATIENT);

        when(userAccountRepository.findById(10L))
                .thenReturn(Optional.of(account));

        when(userRoleRepository.findByUserAccountIdOrderByRoleAsc(10L))
                .thenReturn(List.of(
                        doctorRole,
                        patientRole
                ));

        List<Role> roles =
                userAccountService.getRoles(10L);

        assertEquals(2, roles.size());
        assertTrue(roles.contains(Role.DOCTOR));
        assertTrue(roles.contains(Role.PATIENT));
    }

    @Test
    void removeRole_shouldRemoveAssignedRole() {

        Person person = new Person(
                "John",
                "Doe",
                null,
                null
        );
        person.setId(1L);

        UserAccount account = new UserAccount(
                person,
                "john@example.com",
                "hashed-password",
                UserAccountStatus.ACTIVE
        );
        account.setId(10L);

        when(userAccountRepository.findById(10L))
                .thenReturn(Optional.of(account));

        when(userRoleRepository.existsByUserAccountIdAndRole(
                10L,
                Role.DOCTOR
        )).thenReturn(true);

        when(userRoleRepository.findByUserAccountIdOrderByRoleAsc(10L))
                .thenReturn(List.of());

        List<Role> roles =
                userAccountService.removeRole(
                        10L,
                        Role.DOCTOR
                );

        assertTrue(roles.isEmpty());

        verify(userRoleRepository)
                .deleteByUserAccountIdAndRole(
                        10L,
                        Role.DOCTOR
                );
    }

    @Test
    void removeRole_shouldRejectUnassignedRole() {

        Person person = new Person(
                "John",
                "Doe",
                null,
                null
        );
        person.setId(1L);

        UserAccount account = new UserAccount(
                person,
                "john@example.com",
                "hashed-password",
                UserAccountStatus.ACTIVE
        );
        account.setId(10L);

        when(userAccountRepository.findById(10L))
                .thenReturn(Optional.of(account));

        when(userRoleRepository.existsByUserAccountIdAndRole(
                10L,
                Role.DOCTOR
        )).thenReturn(false);

        assertThrows(
                UserRoleNotAssignedException.class,
                () -> userAccountService.removeRole(
                        10L,
                        Role.DOCTOR
                )
        );

        verify(userRoleRepository, never())
                .deleteByUserAccountIdAndRole(
                        anyLong(),
                        any(Role.class)
                );
    }
}