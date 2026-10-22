package com.coderaah.medtrack.identity.controller;

import com.coderaah.medtrack.identity.domain.Role;
import com.coderaah.medtrack.identity.dto.AssignRoleRequest;
import com.coderaah.medtrack.identity.dto.ChangeUserAccountStatusRequest;
import com.coderaah.medtrack.identity.dto.CreateUserAccountRequest;
import com.coderaah.medtrack.identity.dto.UserAccountResponse;
import com.coderaah.medtrack.identity.service.UserAccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserAccountController {

    private final UserAccountService userAccountService;

    public UserAccountController(UserAccountService userAccountService) {
        this.userAccountService = userAccountService;
    }

    @PostMapping
    public ResponseEntity<UserAccountResponse> createUserAccount(
            @Valid @RequestBody CreateUserAccountRequest request) {

        UserAccountResponse response =
                userAccountService.createUserAccount(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserAccountResponse> getUserAccountById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userAccountService.getUserAccountById(id)
        );
    }

    @GetMapping
    public ResponseEntity<List<UserAccountResponse>> getAllUserAccounts() {

        return ResponseEntity.ok(
                userAccountService.getAllUserAccounts()
        );
    }

    @GetMapping("/by-email")
    public ResponseEntity<UserAccountResponse> getUserAccountByEmail(
            @RequestParam String email) {

        return ResponseEntity.ok(
                userAccountService.getUserAccountByEmail(email)
        );
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<UserAccountResponse> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ChangeUserAccountStatusRequest request) {

        return ResponseEntity.ok(
                userAccountService.changeStatus(id, request)
        );
    }

    @PostMapping("/{id}/roles")
    public ResponseEntity<List<Role>> assignRole(
            @PathVariable Long id,
            @Valid @RequestBody AssignRoleRequest request) {

        return ResponseEntity.ok(
                userAccountService.assignRole(id, request)
        );
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<List<Role>> getRoles(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                userAccountService.getRoles(id)
        );
    }

    @DeleteMapping("/{id}/roles/{role}")
    public ResponseEntity<List<Role>> removeRole(
            @PathVariable Long id,
            @PathVariable Role role) {

        return ResponseEntity.ok(
                userAccountService.removeRole(id, role)
        );
    }
}