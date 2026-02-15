package com.copro.connect.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests JwtUtils")
class JwtUtilsTest {

    private JwtUtils jwtUtils;

    @Mock
    private Authentication authentication;

    private static final String SECRET = "MaCleSecreteTresLonguePourTestHMAC256Minimum";
    private static final long EXPIRATION_MS = 86400000L; // 24h

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "jwtSecret", SECRET);
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", EXPIRATION_MS);
    }

    @Test
    @DisplayName("generateToken(String) produit un JWT valide")
    void generateToken_withUsername_producesValidToken() {
        String token = jwtUtils.generateToken("admin");

        assertThat(token).isNotBlank();
        assertThat(jwtUtils.getUsernameFromToken(token)).isEqualTo("admin");
        assertThat(jwtUtils.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("generateToken(Authentication) produit un JWT avec le username du principal")
    void generateToken_withAuthentication_producesValidToken() {
        when(authentication.getPrincipal()).thenReturn(new org.springframework.security.core.userdetails.User(
                "admin", "pass", java.util.List.of()
        ));

        String token = jwtUtils.generateToken(authentication);

        assertThat(token).isNotBlank();
        assertThat(jwtUtils.getUsernameFromToken(token)).isEqualTo("admin");
        assertThat(jwtUtils.validateToken(token)).isTrue();
    }

    @Test
    @DisplayName("validateToken retourne false pour token invalide")
    void validateToken_withInvalidToken_returnsFalse() {
        assertThat(jwtUtils.validateToken("invalid-token")).isFalse();
        assertThat(jwtUtils.validateToken("")).isFalse();
    }

    @Test
    @DisplayName("getUsernameFromToken extrait le subject")
    void getUsernameFromToken_returnsUsername() {
        String token = jwtUtils.generateToken("user123");
        assertThat(jwtUtils.getUsernameFromToken(token)).isEqualTo("user123");
    }
}
