package com.copro.connect.service;

import com.copro.connect.dto.AdminUserResponse;
import com.copro.connect.model.User;
import com.copro.connect.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service métier des opérations d'administration :
 * - Liste des comptes utilisateurs
 * - Activation / désactivation du MFA par utilisateur
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;

    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "username"))
                .stream()
                .map(AdminUserResponse::fromUser)
                .toList();
    }

    public AdminUserResponse updateMfa(String userId, boolean mfaEnabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));

        log.info("Admin updating MFA for user '{}' -> {}", user.getUsername(), mfaEnabled);
        user.setMfaEnabled(mfaEnabled);
        User saved = userRepository.save(user);
        return AdminUserResponse.fromUser(saved);
    }
}
