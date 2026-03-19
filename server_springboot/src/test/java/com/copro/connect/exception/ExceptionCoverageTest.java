package com.copro.connect.exception;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests de couverture des exceptions")
class ExceptionCoverageTest {

    @Test
    @DisplayName("InvalidMfaCodeException - constructeur avec message")
    void invalidMfaCodeException_message() {
        InvalidMfaCodeException ex = new InvalidMfaCodeException("Code invalide");
        assertThat(ex.getMessage()).isEqualTo("Code invalide");
    }

    @Test
    @DisplayName("InvalidMfaCodeException - constructeur avec message et cause")
    void invalidMfaCodeException_messageAndCause() {
        Throwable cause = new RuntimeException("root cause");
        InvalidMfaCodeException ex = new InvalidMfaCodeException("Code invalide", cause);
        assertThat(ex.getMessage()).isEqualTo("Code invalide");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("EmailSendException - constructeur avec message")
    void emailSendException_message() {
        EmailSendException ex = new EmailSendException("Échec envoi");
        assertThat(ex.getMessage()).isEqualTo("Échec envoi");
    }

    @Test
    @DisplayName("EmailSendException - constructeur avec message et cause")
    void emailSendException_messageAndCause() {
        Throwable cause = new RuntimeException("SMTP error");
        EmailSendException ex = new EmailSendException("Échec envoi", cause);
        assertThat(ex.getMessage()).isEqualTo("Échec envoi");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("ResidentNotFoundException - constructeur avec cause")
    void residentNotFoundException_messageAndCause() {
        Throwable cause = new RuntimeException("DB error");
        ResidentNotFoundException ex = new ResidentNotFoundException("Not found", cause);
        assertThat(ex.getMessage()).isEqualTo("Not found");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("DuplicateResidentException - constructeur avec cause")
    void duplicateResidentException_messageAndCause() {
        Throwable cause = new RuntimeException("DB error");
        DuplicateResidentException ex = new DuplicateResidentException("Duplicate", cause);
        assertThat(ex.getMessage()).isEqualTo("Duplicate");
        assertThat(ex.getCause()).isEqualTo(cause);
    }

    @Test
    @DisplayName("ValidationException - constructeur avec cause")
    void validationException_messageAndCause() {
        Throwable cause = new RuntimeException("Validation error");
        ValidationException ex = new ValidationException("Invalid", cause);
        assertThat(ex.getMessage()).isEqualTo("Invalid");
        assertThat(ex.getCause()).isEqualTo(cause);
    }
}
