package com.copro.connect.service;

import com.copro.connect.model.MfaCode;
import com.copro.connect.model.User;
import com.copro.connect.repository.MfaCodeRepository;
import com.copro.connect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MfaService {

    private final MfaCodeRepository mfaCodeRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final int MAX_ATTEMPTS = 5;

    @Value("${mfa.code.length:6}")
    private int codeLength;

    @Value("${mfa.code.expiration-minutes:5}")
    private int expirationMinutes;

    /**
     * Generates and sends an MFA code by email
     */
    public void generateAndSendCode(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new RuntimeException("No email address configured for this user");
        }

        // Delete old unused codes
        mfaCodeRepository.deleteAllByUsername(username);

        // Generate a new code
        String code = generateNumericCode();

        MfaCode mfaCode = new MfaCode();
        mfaCode.setUsername(username);
        mfaCode.setCode(code);
        mfaCode.setAttempts(0);
        mfaCode.setCreatedAt(Instant.now());
        mfaCode.setExpiresAt(Instant.now().plus(expirationMinutes, ChronoUnit.MINUTES));
        mfaCode.setUsed(false);

        mfaCodeRepository.save(mfaCode);

        // Send the code by email
        emailService.sendMfaCode(user.getEmail(), code, user.getName() != null ? user.getName() : username);

        log.info("MFA code generated and sent for user: {}", username);
    }

    /**
     * Verifies the MFA code
     * @return true if the code is valid
     */
    public boolean verifyCode(String username, String code) {
        MfaCode mfaCode = mfaCodeRepository
                .findTopByUsernameAndUsedFalseOrderByCreatedAtDesc(username)
                .orElse(null);

        if (mfaCode == null) {
            log.warn("No MFA code found for user: {}", username);
            return false;
        }

        // Check expiration
        if (Instant.now().isAfter(mfaCode.getExpiresAt())) {
            log.warn("MFA code expired for user: {}", username);
            mfaCode.setUsed(true);
            mfaCodeRepository.save(mfaCode);
            return false;
        }

        // Check number of attempts
        if (mfaCode.getAttempts() >= MAX_ATTEMPTS) {
            log.warn("Too many MFA attempts for user: {}", username);
            mfaCode.setUsed(true);
            mfaCodeRepository.save(mfaCode);
            return false;
        }

        // Increment attempts
        mfaCode.setAttempts(mfaCode.getAttempts() + 1);

        // Verify the code
        if (mfaCode.getCode().equals(code.trim())) {
            mfaCode.setUsed(true);
            mfaCodeRepository.save(mfaCode);
            log.info("MFA code verified successfully for user: {}", username);
            return true;
        }

        mfaCodeRepository.save(mfaCode);
        log.warn("Invalid MFA code attempt ({}/{}) for user: {}", mfaCode.getAttempts(), MAX_ATTEMPTS, username);
        return false;
    }

    /**
     * Masks the email for frontend display (e.g. ad***@gmail.com)
     */
    public String getMaskedEmail(String username) {
        return userRepository.findByUsername(username)
                .map(user -> {
                    String email = user.getEmail();
                    if (email == null || !email.contains("@")) return "***";
                    String[] parts = email.split("@");
                    String name = parts[0];
                    if (name.length() <= 2) return name.charAt(0) + "***@" + parts[1];
                    return name.substring(0, 2) + "***@" + parts[1];
                })
                .orElse("***");
    }

    private String generateNumericCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < codeLength; i++) {
            sb.append(RANDOM.nextInt(10));
        }
        return sb.toString();
    }
}
