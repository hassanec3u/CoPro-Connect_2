package com.copro.connect.service;

import com.copro.connect.dto.LoginRequest;
import com.copro.connect.dto.LoginResponse;
import com.copro.connect.model.User;
import com.copro.connect.repository.UserRepository;
import com.copro.connect.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests AuthService")
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private AuthService authService;

    private User user;
    private LoginRequest loginRequest;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId("user-1");
        user.setUsername("admin");
        user.setPassword("encoded");
        user.setName("Admin");
        user.setRole("ADMIN");

        loginRequest = new LoginRequest("admin", "password");
        authentication = new UsernamePasswordAuthenticationToken(user.getUsername(), null, user.getAuthorities());
    }

    @Test
    @DisplayName("login authentifie et retourne token + userInfo")
    void login_shouldReturnTokenAndUserInfo() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateToken(authentication)).thenReturn("jwt-token-123");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        LoginResponse response = authService.login(loginRequest);

        assertThat(response.getToken()).isEqualTo("jwt-token-123");
        assertThat(response.getUser()).isNotNull();
        assertThat(response.getUser().getUsername()).isEqualTo("admin");
        assertThat(response.getUser().getName()).isEqualTo("Admin");
        assertThat(response.getUser().getRole()).isEqualTo("ADMIN");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtils).generateToken(authentication);
        verify(userRepository).findByUsername("admin");
    }

    @Test
    @DisplayName("login lance exception si user non trouvé après auth")
    void login_whenUserNotFoundAfterAuth_throws() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtUtils.generateToken(authentication)).thenReturn("jwt-token");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("User not found");

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(userRepository).findByUsername("admin");
    }
}
