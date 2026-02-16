package com.copro.connect.validator;

import com.copro.connect.exception.DuplicateResidentException;
import com.copro.connect.exception.ValidationException;
import com.copro.connect.model.Resident;
import com.copro.connect.repository.ResidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests ResidentValidator")
class ResidentValidatorTest {

    @Mock
    private ResidentRepository residentRepository;

    @InjectMocks
    private ResidentValidator residentValidator;

    private Resident resident;

    @BeforeEach
    void setUp() {
        resident = new Resident();
        resident.setId("res-1");
        resident.setLotId("LOT-001");
        resident.setBatiment("A");
        resident.setEtage("1");
        resident.setPorte("101");
        resident.setProprietaireNom("Dupont");
    }

    @Test
    @DisplayName("validateForCreation OK quand lotId unique")
    void validateForCreation_whenLotIdUnique_ok() {
        when(residentRepository.findByLotIdIgnoreCase("LOT-001")).thenReturn(Optional.empty());

        assertThatCode(() -> residentValidator.validateForCreation(resident)).doesNotThrowAnyException();
        verify(residentRepository).findByLotIdIgnoreCase("LOT-001");
    }

    @Test
    @DisplayName("validateForCreation lance DuplicateResidentException si lotId existe")
    void validateForCreation_whenLotIdExists_throws() {
        when(residentRepository.findByLotIdIgnoreCase("LOT-001")).thenReturn(Optional.of(resident));

        assertThatThrownBy(() -> residentValidator.validateForCreation(resident))
                .isInstanceOf(DuplicateResidentException.class)
                .hasMessageContaining("LOT-001");
        verify(residentRepository).findByLotIdIgnoreCase("LOT-001");
    }

    @Test
    @DisplayName("validateForCreation OK si lotId null ou vide")
    void validateForCreation_whenLotIdEmpty_ok() {
        resident.setLotId(""); // pas d'appel repository quand lotId vide

        assertThatCode(() -> residentValidator.validateForCreation(resident)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateForUpdate OK quand id valide et résident existe")
    void validateForUpdate_whenValid_ok() {
        when(residentRepository.findById("res-1")).thenReturn(Optional.of(resident));

        Resident details = new Resident();
        details.setLotId("LOT-001"); // même lotId que existing -> pas de vérif doublon

        assertThatCode(() -> residentValidator.validateForUpdate("res-1", details)).doesNotThrowAnyException();
        verify(residentRepository).findById("res-1");
        verify(residentRepository, never()).findByLotIdIgnoreCase(any());
    }

    @Test
    @DisplayName("validateForUpdate lance ValidationException si id vide")
    void validateForUpdate_whenIdEmpty_throws() {
        assertThatThrownBy(() -> residentValidator.validateForUpdate("", resident))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("vide");
        verify(residentRepository, never()).findById(any());
    }

    @Test
    @DisplayName("validateForUpdate lance ValidationException si résident inexistant")
    void validateForUpdate_whenResidentNotFound_throws() {
        when(residentRepository.findById("inconnu")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> residentValidator.validateForUpdate("inconnu", resident))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("n'existe pas");
        verify(residentRepository).findById("inconnu");
    }

    @Test
    @DisplayName("validateForUpdate lance DuplicateResidentException si nouveau lotId déjà pris")
    void validateForUpdate_whenNewLotIdTaken_throws() {
        Resident other = new Resident();
        other.setId("res-2");
        other.setLotId("LOT-002");
        when(residentRepository.findById("res-1")).thenReturn(Optional.of(resident));
        when(residentRepository.findByLotIdIgnoreCase("LOT-002")).thenReturn(Optional.of(other));

        Resident details = new Resident();
        details.setLotId("LOT-002");

        assertThatThrownBy(() -> residentValidator.validateForUpdate("res-1", details))
                .isInstanceOf(DuplicateResidentException.class)
                .hasMessageContaining("LOT-002");
        verify(residentRepository).findByLotIdIgnoreCase("LOT-002");
    }

    @Test
    @DisplayName("validateId OK pour id non vide")
    void validateId_whenValid_ok() {
        assertThatCode(() -> residentValidator.validateId("res-1")).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("validateId lance ValidationException si id null ou vide")
    void validateId_whenEmpty_throws() {
        assertThatThrownBy(() -> residentValidator.validateId(null))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("vide");
        assertThatThrownBy(() -> residentValidator.validateId(""))
                .isInstanceOf(ValidationException.class);
        assertThatThrownBy(() -> residentValidator.validateId("   "))
                .isInstanceOf(ValidationException.class);
    }

    @Test
    @DisplayName("validateSearchParameter lance exception si valeur vide (après trim)")
    void validateSearchParameter_whenEmpty_throws() {
        assertThatThrownBy(() -> residentValidator.validateSearchParameter("search", "   "))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("search");
    }

    @Test
    @DisplayName("validateSearchParameter OK si valeur null")
    void validateSearchParameter_whenNull_ok() {
        assertThatCode(() -> residentValidator.validateSearchParameter("search", null)).doesNotThrowAnyException();
    }
}
