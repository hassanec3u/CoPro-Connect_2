package com.copro.connect.controller;

import com.copro.connect.dto.AdminUserResponse;
import com.copro.connect.dto.UpdateMfaRequest;
import com.copro.connect.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Endpoints réservés aux administrateurs (role = ADMIN).
 */
@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> listUsers() {
        log.info("Admin request: list users");
        return ResponseEntity.ok(adminService.listUsers());
    }

    @PatchMapping("/users/{id}/mfa")
    public ResponseEntity<AdminUserResponse> updateMfa(
            @PathVariable("id") String id,
            @Valid @RequestBody UpdateMfaRequest request) {
        log.info("Admin request: update MFA user={} enabled={}", id, request.getMfaEnabled());
        AdminUserResponse updated = adminService.updateMfa(id, Boolean.TRUE.equals(request.getMfaEnabled()));
        return ResponseEntity.ok(updated);
    }
}
