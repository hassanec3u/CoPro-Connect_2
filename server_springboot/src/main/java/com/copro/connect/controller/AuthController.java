package com.copro.connect.controller;

import com.copro.connect.dto.LoginRequest;
import com.copro.connect.dto.LoginResponse;
import com.copro.connect.dto.MfaVerifyRequest;
import com.copro.connect.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Login request received for user: {}", loginRequest.getUsername());
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/verify-mfa")
    public ResponseEntity<LoginResponse> verifyMfa(@Valid @RequestBody MfaVerifyRequest request) {
        log.info("MFA verification request for user: {}", request.getUsername());
        LoginResponse response = authService.verifyMfa(request);
        return ResponseEntity.ok(response);
    }
}
