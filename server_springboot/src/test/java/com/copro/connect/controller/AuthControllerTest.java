package com.copro.connect.controller;

import com.copro.connect.dto.LoginRequest;
import com.copro.connect.dto.LoginResponse;
import com.copro.connect.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests AuthController")
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    @DisplayName("login retourne token et userInfo")
    void login_returnsTokenAndUser() {
        LoginResponse response = LoginResponse.success("jwt-token-123",
                new com.copro.connect.model.User("user-1", "admin", "password", "Admin", "admin@test.fr", "ADMIN", true, null, null));
        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        LoginRequest request = new LoginRequest("admin", "password");
        ResponseEntity<LoginResponse> result = authController.login(request);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getToken()).isEqualTo("jwt-token-123");
        assertThat(result.getBody().getUser().getUsername()).isEqualTo("admin");
        assertThat(result.getBody().getUser().getName()).isEqualTo("Admin");
        verify(authService).login(any(LoginRequest.class));
    }
}
