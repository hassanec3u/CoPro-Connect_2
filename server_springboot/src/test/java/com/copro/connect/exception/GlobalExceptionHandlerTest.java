package com.copro.connect.exception;

import com.copro.connect.controller.ResidentController;
import com.copro.connect.dto.ErrorResponse;
import com.copro.connect.model.Resident;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests GlobalExceptionHandler")
class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @Mock
    private WebRequest webRequest;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    private void stubWebRequest() {
        when(webRequest.getDescription(false)).thenReturn("uri=/api/residents");
    }

    @Test
    @DisplayName("handleBadCredentials retourne 401")
    void handleBadCredentials_returns401() {
        stubWebRequest();
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        ResponseEntity<ErrorResponse> response = handler.handleBadCredentials(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid credentials");
        assertThat(response.getBody().getStatus()).isEqualTo(401);
    }

    @Test
    @DisplayName("handleUsernameNotFound retourne 401")
    void handleUsernameNotFound_returns401() {
        stubWebRequest();
        UsernameNotFoundException ex = new UsernameNotFoundException("User not found: admin");

        ResponseEntity<ErrorResponse> response = handler.handleUsernameNotFound(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Invalid credentials");
    }

    @Test
    @DisplayName("handleResidentNotFound retourne 404")
    void handleResidentNotFound_returns404() {
        stubWebRequest();
        ResidentNotFoundException ex = new ResidentNotFoundException("res-1");

        ResponseEntity<ErrorResponse> response = handler.handleResidentNotFound(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("res-1");
        assertThat(response.getBody().getStatus()).isEqualTo(404);
    }

    @Test
    @DisplayName("handleDuplicateResident retourne 409")
    void handleDuplicateResident_returns409() {
        stubWebRequest();
        DuplicateResidentException ex = new DuplicateResidentException("LOT-001");

        ResponseEntity<ErrorResponse> response = handler.handleDuplicateResident(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).contains("LOT-001");
        assertThat(response.getBody().getStatus()).isEqualTo(409);
    }

    @Test
    @DisplayName("handleValidation retourne 400")
    void handleValidation_returns400() {
        stubWebRequest();
        ValidationException ex = new ValidationException("Champ invalide");

        ResponseEntity<ErrorResponse> response = handler.handleValidation(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Champ invalide");
        assertThat(response.getBody().getStatus()).isEqualTo(400);
    }

    @Test
    @DisplayName("handleValidationExceptions pour MethodArgumentNotValidException retourne 400 et errors")
    void handleValidationExceptions_returns400WithErrors() throws Exception {
        MethodArgumentNotValidException ex = mockMethodArgumentNotValidException();

        ResponseEntity<?> response = handler.handleValidationExceptions(ex);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isInstanceOf(java.util.Map.class);
        @SuppressWarnings("unchecked")
        java.util.Map<String, Object> body = (java.util.Map<String, Object>) response.getBody();
        assertThat(body).containsKey("message").containsKey("errors").containsKey("status");
        assertThat(body.get("message")).isEqualTo("Erreur de validation");
        assertThat(body.get("status")).isEqualTo(400);
    }

    @Test
    @DisplayName("handleRuntimeException retourne 500 par défaut")
    void handleRuntimeException_returns500() {
        stubWebRequest();
        RuntimeException ex = new RuntimeException("Erreur inconnue");

        ResponseEntity<ErrorResponse> response = handler.handleRuntimeException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Erreur inconnue");
    }

    @Test
    @DisplayName("handleGlobalException retourne 500")
    void handleGlobalException_returns500() {
        stubWebRequest();
        Exception ex = new Exception("Erreur serveur");

        ResponseEntity<ErrorResponse> response = handler.handleGlobalException(ex, webRequest);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getMessage()).isEqualTo("Erreur serveur interne");
    }

    private MethodArgumentNotValidException mockMethodArgumentNotValidException() throws Exception {
        Resident target = new Resident();
        BindingResult bindingResult = new BeanPropertyBindingResult(target, "resident");
        bindingResult.addError(new FieldError("resident", "lotId", "Le numéro de lot est obligatoire"));
        Method method = ResidentController.class.getMethod("createResident", Resident.class);
        MethodParameter parameter = new MethodParameter(method, 0);
        return new MethodArgumentNotValidException(parameter, bindingResult);
    }
}
