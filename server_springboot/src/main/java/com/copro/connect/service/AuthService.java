package com.copro.connect.service;

import com.copro.connect.dto.LoginRequest;
import com.copro.connect.dto.LoginResponse;
import com.copro.connect.dto.MfaVerifyRequest;
import com.copro.connect.exception.InvalidMfaCodeException;
import com.copro.connect.model.User;
import com.copro.connect.repository.UserRepository;
import com.copro.connect.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final MfaService mfaService;

    /**
     * Étape 1 : Vérifie les identifiants.
     * Si MFA activé -> génère et envoie le code, retourne mfa_required=true
     * Si MFA désactivé -> retourne directement le JWT
     */
    public LoginResponse login(LoginRequest loginRequest) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());

        // Authentifier les identifiants (username/password)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Si MFA activé et email configuré -> envoyer le code
        if (user.isMfaEnabled() && user.getEmail() != null && !user.getEmail().isBlank()) {
            log.info("MFA required for user: {}", user.getUsername());
            mfaService.generateAndSendCode(user.getUsername());
            String maskedEmail = mfaService.getMaskedEmail(user.getUsername());
            return LoginResponse.mfaRequired(maskedEmail);
        }

        // Pas de MFA -> connexion directe
        log.info("Login successful (no MFA) for user: {}", user.getUsername());
        String jwt = jwtUtils.generateToken(user.getUsername());
        return LoginResponse.success(jwt, user);
    }

    /**
     * Étape 2 : Vérifie le code MFA et retourne le JWT si valide
     */
    public LoginResponse verifyMfa(MfaVerifyRequest request) {
        log.info("MFA verification attempt for user: {}", request.getUsername());

        boolean valid = mfaService.verifyCode(request.getUsername(), request.getCode());

        if (!valid) {
            log.warn("MFA verification failed for user: {}", request.getUsername());
            throw new InvalidMfaCodeException("Code de vérification invalide ou expiré");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        String jwt = jwtUtils.generateToken(user.getUsername());

        // Positionner le SecurityContext
        Authentication auth = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        log.info("MFA verification successful for user: {}", user.getUsername());
        return LoginResponse.success(jwt, user);
    }
}
