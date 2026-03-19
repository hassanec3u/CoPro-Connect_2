package com.copro.connect.service;

import com.copro.connect.model.MfaCode;
import com.copro.connect.model.User;
import com.copro.connect.repository.MfaCodeRepository;
import com.copro.connect.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests MfaService")
class MfaServiceTest {

    @Mock
    private MfaCodeRepository mfaCodeRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private MfaService mfaService;

    private User user;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mfaService, "codeLength", 6);
        ReflectionTestUtils.setField(mfaService, "expirationMinutes", 5);

        user = new User();
        user.setUsername("admin");
        user.setEmail("admin@copro.fr");
        user.setName("Admin");
    }

    // ==================== generateAndSendCode ====================

    @Test
    @DisplayName("generateAndSendCode lance exception si utilisateur introuvable")
    void generateAndSendCode_userNotFound_throws() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mfaService.generateAndSendCode("unknown"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("introuvable");
    }

    @Test
    @DisplayName("generateAndSendCode lance exception si email absent")
    void generateAndSendCode_noEmail_throws() {
        user.setEmail(null);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> mfaService.generateAndSendCode("admin"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("email");
    }

    @Test
    @DisplayName("generateAndSendCode lance exception si email vide")
    void generateAndSendCode_blankEmail_throws() {
        user.setEmail("   ");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> mfaService.generateAndSendCode("admin"))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @DisplayName("generateAndSendCode génère et envoie un code avec le nom de l'utilisateur")
    void generateAndSendCode_success_withName() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(mfaCodeRepository.save(any(MfaCode.class))).thenAnswer(inv -> inv.getArgument(0));

        mfaService.generateAndSendCode("admin");

        ArgumentCaptor<MfaCode> captor = ArgumentCaptor.forClass(MfaCode.class);
        verify(mfaCodeRepository).save(captor.capture());
        MfaCode saved = captor.getValue();

        assertThat(saved.getUsername()).isEqualTo("admin");
        assertThat(saved.getCode()).hasSize(6);
        assertThat(saved.isUsed()).isFalse();
        assertThat(saved.getAttempts()).isZero();
        verify(emailService).sendMfaCode(eq("admin@copro.fr"), anyString(), eq("Admin"));
        verify(mfaCodeRepository).deleteAllByUsername("admin");
    }

    @Test
    @DisplayName("generateAndSendCode utilise le username si name est null")
    void generateAndSendCode_success_nullName() {
        user.setName(null);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));
        when(mfaCodeRepository.save(any(MfaCode.class))).thenAnswer(inv -> inv.getArgument(0));

        mfaService.generateAndSendCode("admin");

        verify(emailService).sendMfaCode(eq("admin@copro.fr"), anyString(), eq("admin"));
    }

    // ==================== verifyCode ====================

    @Test
    @DisplayName("verifyCode retourne false si aucun code trouvé")
    void verifyCode_noCode_returnsFalse() {
        when(mfaCodeRepository.findTopByUsernameAndUsedFalseOrderByCreatedAtDesc("admin"))
                .thenReturn(Optional.empty());

        boolean result = mfaService.verifyCode("admin", "123456");

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("verifyCode retourne false si code expiré")
    void verifyCode_expired_returnsFalse() {
        MfaCode mfaCode = new MfaCode();
        mfaCode.setCode("123456");
        mfaCode.setAttempts(0);
        mfaCode.setUsed(false);
        mfaCode.setExpiresAt(Instant.now().minusSeconds(60));
        when(mfaCodeRepository.findTopByUsernameAndUsedFalseOrderByCreatedAtDesc("admin"))
                .thenReturn(Optional.of(mfaCode));

        boolean result = mfaService.verifyCode("admin", "123456");

        assertThat(result).isFalse();
        assertThat(mfaCode.isUsed()).isTrue();
        verify(mfaCodeRepository).save(mfaCode);
    }

    @Test
    @DisplayName("verifyCode retourne false si trop de tentatives")
    void verifyCode_tooManyAttempts_returnsFalse() {
        MfaCode mfaCode = new MfaCode();
        mfaCode.setCode("123456");
        mfaCode.setAttempts(5);
        mfaCode.setUsed(false);
        mfaCode.setExpiresAt(Instant.now().plusSeconds(300));
        when(mfaCodeRepository.findTopByUsernameAndUsedFalseOrderByCreatedAtDesc("admin"))
                .thenReturn(Optional.of(mfaCode));

        boolean result = mfaService.verifyCode("admin", "123456");

        assertThat(result).isFalse();
        assertThat(mfaCode.isUsed()).isTrue();
    }

    @Test
    @DisplayName("verifyCode retourne false si code incorrect")
    void verifyCode_wrongCode_returnsFalse() {
        MfaCode mfaCode = new MfaCode();
        mfaCode.setCode("123456");
        mfaCode.setAttempts(0);
        mfaCode.setUsed(false);
        mfaCode.setExpiresAt(Instant.now().plusSeconds(300));
        when(mfaCodeRepository.findTopByUsernameAndUsedFalseOrderByCreatedAtDesc("admin"))
                .thenReturn(Optional.of(mfaCode));

        boolean result = mfaService.verifyCode("admin", "999999");

        assertThat(result).isFalse();
        assertThat(mfaCode.getAttempts()).isEqualTo(1);
        verify(mfaCodeRepository).save(mfaCode);
    }

    @Test
    @DisplayName("verifyCode retourne true si code valide")
    void verifyCode_validCode_returnsTrue() {
        MfaCode mfaCode = new MfaCode();
        mfaCode.setCode("123456");
        mfaCode.setAttempts(1);
        mfaCode.setUsed(false);
        mfaCode.setExpiresAt(Instant.now().plusSeconds(300));
        when(mfaCodeRepository.findTopByUsernameAndUsedFalseOrderByCreatedAtDesc("admin"))
                .thenReturn(Optional.of(mfaCode));

        boolean result = mfaService.verifyCode("admin", "123456");

        assertThat(result).isTrue();
        assertThat(mfaCode.isUsed()).isTrue();
        verify(mfaCodeRepository).save(mfaCode);
    }

    @Test
    @DisplayName("verifyCode accepte code avec espaces autour (trim)")
    void verifyCode_codeWithSpaces_returnsTrue() {
        MfaCode mfaCode = new MfaCode();
        mfaCode.setCode("123456");
        mfaCode.setAttempts(0);
        mfaCode.setUsed(false);
        mfaCode.setExpiresAt(Instant.now().plusSeconds(300));
        when(mfaCodeRepository.findTopByUsernameAndUsedFalseOrderByCreatedAtDesc("admin"))
                .thenReturn(Optional.of(mfaCode));

        boolean result = mfaService.verifyCode("admin", "  123456  ");

        assertThat(result).isTrue();
    }

    // ==================== getMaskedEmail ====================

    @Test
    @DisplayName("getMaskedEmail retourne *** si utilisateur non trouvé")
    void getMaskedEmail_userNotFound_returns3Stars() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        String result = mfaService.getMaskedEmail("unknown");

        assertThat(result).isEqualTo("***");
    }

    @Test
    @DisplayName("getMaskedEmail masque correctement un email normal")
    void getMaskedEmail_normalEmail_isMasked() {
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        String result = mfaService.getMaskedEmail("admin");

        assertThat(result).isEqualTo("ad***@copro.fr");
    }

    @Test
    @DisplayName("getMaskedEmail masque email avec local part court (≤2)")
    void getMaskedEmail_shortEmail_isMasked() {
        user.setEmail("ab@copro.fr");
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        String result = mfaService.getMaskedEmail("admin");

        assertThat(result).isEqualTo("a***@copro.fr");
    }

    @Test
    @DisplayName("getMaskedEmail retourne *** si email null")
    void getMaskedEmail_nullEmail_returns3Stars() {
        user.setEmail(null);
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(user));

        String result = mfaService.getMaskedEmail("admin");

        assertThat(result).isEqualTo("***");
    }
}
