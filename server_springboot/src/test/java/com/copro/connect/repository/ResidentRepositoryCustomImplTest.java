package com.copro.connect.repository;

import com.copro.connect.dto.StatisticsResponse;
import com.copro.connect.model.HappixAccount;
import com.copro.connect.model.Occupant;
import com.copro.connect.model.Resident;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Tests ResidentRepositoryCustomImpl")
class ResidentRepositoryCustomImplTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @InjectMocks
    private ResidentRepositoryCustomImpl residentRepositoryCustom;

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
        resident.setStatutLot("Propriétaire Résident");
        resident.setOccupants(List.of(new Occupant("Jean", "0612345678", "jean@test.com")));
        HappixAccount happix = new HappixAccount();
        happix.setNom("Compte Happix");
        happix.setType("resident");
        resident.setHappixAccounts(List.of(happix));
    }

    // --- findWithFilters ---

    @Test
    @DisplayName("findWithFilters sans filtres appelle count et find avec une query sans critères")
    void findWithFilters_noFilters_callsCountAndFind() {
        when(mongoTemplate.count(any(Query.class), eq(Resident.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Resident.class))).thenReturn(List.of(resident));

        Pageable pageable = PageRequest.of(0, 10);
        Page<Resident> result = residentRepositoryCustom.findWithFilters(null, null, null, pageable);

        assertThat(result.getContent()).hasSize(1).containsExactly(resident);
        assertThat(result.getTotalElements()).isOne();
        assertThat(result.getTotalPages()).isOne();
        assertThat(result.getNumber()).isZero();
        assertThat(result.getSize()).isEqualTo(10);

        ArgumentCaptor<Query> queryCaptor = ArgumentCaptor.forClass(Query.class);
        verify(mongoTemplate).count(queryCaptor.capture(), eq(Resident.class));
        verify(mongoTemplate).find(queryCaptor.capture(), eq(Resident.class));
        assertThat(queryCaptor.getAllValues()).hasSize(2);
    }

    @Test
    @DisplayName("findWithFilters avec search appelle count et find")
    void findWithFilters_withSearch_callsCountAndFind() {
        when(mongoTemplate.count(any(Query.class), eq(Resident.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Resident.class))).thenReturn(List.of(resident));

        Page<Resident> result = residentRepositoryCustom.findWithFilters("Dupont", null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        verify(mongoTemplate).count(any(Query.class), eq(Resident.class));
        verify(mongoTemplate).find(any(Query.class), eq(Resident.class));
    }

    @Test
    @DisplayName("findWithFilters avec batiment appelle count et find")
    void findWithFilters_withBatiment_callsCountAndFind() {
        when(mongoTemplate.count(any(Query.class), eq(Resident.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Resident.class))).thenReturn(List.of(resident));

        Page<Resident> result = residentRepositoryCustom.findWithFilters(null, "A", null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        verify(mongoTemplate).count(any(Query.class), eq(Resident.class));
        verify(mongoTemplate).find(any(Query.class), eq(Resident.class));
    }

    @Test
    @DisplayName("findWithFilters avec statutLot appelle count et find")
    void findWithFilters_withStatutLot_callsCountAndFind() {
        when(mongoTemplate.count(any(Query.class), eq(Resident.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Resident.class))).thenReturn(List.of(resident));

        Page<Resident> result = residentRepositoryCustom.findWithFilters(
                null, null, "Propriétaire Résident", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        verify(mongoTemplate).count(any(Query.class), eq(Resident.class));
        verify(mongoTemplate).find(any(Query.class), eq(Resident.class));
    }

    @Test
    @DisplayName("findWithFilters avec Pageable trié conserve le tri")
    void findWithFilters_withSort_usesPageableSort() {
        when(mongoTemplate.count(any(Query.class), eq(Resident.class))).thenReturn(0L);
        when(mongoTemplate.find(any(Query.class), eq(Resident.class))).thenReturn(List.of());

        Pageable pageable = PageRequest.of(0, 5, Sort.by("lotId").ascending());
        Page<Resident> result = residentRepositoryCustom.findWithFilters(null, null, null, pageable);

        assertThat(result.getContent()).isEmpty();
        assertThat(result.getPageable().getSort().getOrderFor("lotId")).isNotNull();
    }

    @Test
    @DisplayName("findWithFilters avec search vide ne filtre pas par search")
    void findWithFilters_emptySearch_noSearchCriteria() {
        when(mongoTemplate.count(any(Query.class), eq(Resident.class))).thenReturn(1L);
        when(mongoTemplate.find(any(Query.class), eq(Resident.class))).thenReturn(List.of(resident));

        Page<Resident> result = residentRepositoryCustom.findWithFilters("   ", null, null, PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        verify(mongoTemplate).count(any(Query.class), eq(Resident.class));
    }

    // --- calculateStatistics ---

    @Test
    @DisplayName("calculateStatistics avec liste vide retourne des zéros")
    void calculateStatistics_emptyList_returnsZeros() {
        when(mongoTemplate.findAll(Resident.class)).thenReturn(List.of());

        StatisticsResponse stats = residentRepositoryCustom.calculateStatistics();

        assertThat(stats.getTotalLots()).isZero();
        assertThat(stats.getTotalBatiments()).isZero();
        assertThat(stats.getTotalOccupants()).isZero();
        assertThat(stats.getTotalHappix()).isZero();
        assertThat(stats.getLotsAvecOccupants()).isZero();
        assertThat(stats.getLotsVides()).isZero();
        assertThat(stats.getMoyenneOccupants()).isEqualTo(0.0);
        assertThat(stats.getStatutCount()).isEmpty();
        assertThat(stats.getBatimentCount()).isEmpty();
        assertThat(stats.getHappixByType()).isEmpty();
        verify(mongoTemplate).findAll(Resident.class);
    }

    @Test
    @DisplayName("calculateStatistics avec un résident calcule les stats correctement")
    void calculateStatistics_oneResident_calculatesCorrectly() {
        when(mongoTemplate.findAll(Resident.class)).thenReturn(List.of(resident));

        StatisticsResponse stats = residentRepositoryCustom.calculateStatistics();

        assertThat(stats.getTotalLots()).isOne();
        assertThat(stats.getTotalBatiments()).isOne();
        assertThat(stats.getTotalOccupants()).isOne();
        assertThat(stats.getTotalHappix()).isOne();
        assertThat(stats.getLotsAvecOccupants()).isOne();
        assertThat(stats.getLotsVides()).isZero();
        assertThat(stats.getMoyenneOccupants()).isEqualTo(1.0);
        assertThat(stats.getStatutCount()).containsEntry("Propriétaire Résident", 1L);
        assertThat(stats.getBatimentCount()).containsEntry("A", 1L);
        assertThat(stats.getHappixByType()).containsEntry("resident", 1L);
    }

    @Test
    @DisplayName("calculateStatistics avec plusieurs résidents agrège statuts et bâtiments")
    void calculateStatistics_multipleResidents_aggregatesCorrectly() {
        Resident r2 = new Resident();
        r2.setId("res-2");
        r2.setBatiment("A");
        r2.setStatutLot("Propriétaire Résident");
        r2.setOccupants(null);
        r2.setHappixAccounts(null);

        Resident r3 = new Resident();
        r3.setId("res-3");
        r3.setBatiment("B");
        r3.setStatutLot("Propriétaire Bailleur");
        r3.setOccupants(List.of(new Occupant("Marie", null, null)));
        r3.setHappixAccounts(null);

        when(mongoTemplate.findAll(Resident.class)).thenReturn(List.of(resident, r2, r3));

        StatisticsResponse stats = residentRepositoryCustom.calculateStatistics();

        assertThat(stats.getTotalLots()).isEqualTo(3L);
        assertThat(stats.getTotalBatiments()).isEqualTo(2L); // A, B
        assertThat(stats.getTotalOccupants()).isEqualTo(2L); // 1 + 0 + 1
        assertThat(stats.getTotalHappix()).isOne();
        assertThat(stats.getLotsAvecOccupants()).isEqualTo(2L);
        assertThat(stats.getLotsVides()).isOne();
        assertThat(stats.getMoyenneOccupants()).isEqualTo(0.7); // 2/3 arrondi
        assertThat(stats.getStatutCount()).containsEntry("Propriétaire Résident", 2L);
        assertThat(stats.getStatutCount()).containsEntry("Propriétaire Bailleur", 1L);
        assertThat(stats.getBatimentCount()).containsEntry("A", 2L);
        assertThat(stats.getBatimentCount()).containsEntry("B", 1L);
    }

    @Test
    @DisplayName("calculateStatistics avec statut null utilise Non défini")
    void calculateStatistics_nullStatut_usesNonDefini() {
        resident.setStatutLot(null);
        resident.setBatiment(null);
        when(mongoTemplate.findAll(Resident.class)).thenReturn(List.of(resident));

        StatisticsResponse stats = residentRepositoryCustom.calculateStatistics();

        assertThat(stats.getStatutCount()).containsEntry("Non défini", 1L);
        assertThat(stats.getBatimentCount()).containsEntry("Non défini", 1L);
    }

    @Test
    @DisplayName("calculateStatistics avec Happix sans type utilise Non défini")
    void calculateStatistics_happixWithoutType_usesNonDefini() {
        resident.getHappixAccounts().get(0).setType(null);
        when(mongoTemplate.findAll(Resident.class)).thenReturn(List.of(resident));

        StatisticsResponse stats = residentRepositoryCustom.calculateStatistics();

        assertThat(stats.getHappixByType()).containsEntry("Non défini", 1L);
    }

    // --- findByLotIdIgnoreCase ---

    @Test
    @DisplayName("findByLotIdIgnoreCase trouve un résident avec lotId exact")
    void findByLotIdIgnoreCase_exactMatch_returnsResident() {
        when(mongoTemplate.findOne(any(Query.class), eq(Resident.class))).thenReturn(resident);

        var result = residentRepositoryCustom.findByLotIdIgnoreCase("LOT-001");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(resident);
        verify(mongoTemplate).findOne(any(Query.class), eq(Resident.class));
    }

    @Test
    @DisplayName("findByLotIdIgnoreCase trouve un résident avec lotId en casse différente")
    void findByLotIdIgnoreCase_caseInsensitive_returnsResident() {
        when(mongoTemplate.findOne(any(Query.class), eq(Resident.class))).thenReturn(resident);

        var result = residentRepositoryCustom.findByLotIdIgnoreCase("lot-001");

        assertThat(result).isPresent();
        assertThat(result.get()).isEqualTo(resident);
        verify(mongoTemplate).findOne(any(Query.class), eq(Resident.class));
    }

    @Test
    @DisplayName("findByLotIdIgnoreCase retourne empty si aucun résident trouvé")
    void findByLotIdIgnoreCase_notFound_returnsEmpty() {
        when(mongoTemplate.findOne(any(Query.class), eq(Resident.class))).thenReturn(null);

        var result = residentRepositoryCustom.findByLotIdIgnoreCase("LOT-999");

        assertThat(result).isEmpty();
        verify(mongoTemplate).findOne(any(Query.class), eq(Resident.class));
    }

    @Test
    @DisplayName("findByLotIdIgnoreCase avec lotId null retourne empty")
    void findByLotIdIgnoreCase_nullLotId_returnsEmpty() {
        var result = residentRepositoryCustom.findByLotIdIgnoreCase(null);

        assertThat(result).isEmpty();
        verify(mongoTemplate, never()).findOne(any(Query.class), eq(Resident.class));
    }

    @Test
    @DisplayName("findByLotIdIgnoreCase avec lotId vide retourne empty")
    void findByLotIdIgnoreCase_emptyLotId_returnsEmpty() {
        var result = residentRepositoryCustom.findByLotIdIgnoreCase("   ");

        assertThat(result).isEmpty();
        verify(mongoTemplate, never()).findOne(any(Query.class), eq(Resident.class));
    }
}
