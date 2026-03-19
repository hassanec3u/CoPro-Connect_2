package com.copro.connect.dto;

import com.copro.connect.model.ChangeDetail;
import com.copro.connect.model.ResidentHistory;
import com.copro.connect.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;

@DisplayName("Tests de couverture des DTOs")
class DtoCoverageTest {

    @Test
    @DisplayName("LoginRequest - constructeur, getters et setters")
    void loginRequest_gettersSetters() {
        LoginRequest req = new LoginRequest("admin", "password");
        assertThat(req.getUsername()).isEqualTo("admin");
        assertThat(req.getPassword()).isEqualTo("password");

        req.setUsername("user");
        req.setPassword("secret");
        assertThat(req.getUsername()).isEqualTo("user");
        assertThat(req.getPassword()).isEqualTo("secret");

        LoginRequest req2 = new LoginRequest();
        assertThat(req2.getUsername()).isNull();
        assertThat(req.toString()).isNotNull();
        assertThat(req.hashCode()).isNotZero();
    }

    @Test
    @DisplayName("LoginResponse - success() factory method")
    void loginResponse_success() {
        User user = new User("user-1", "admin", "password", "Admin", "admin@test.com", "ADMIN", true, null, null);
        LoginResponse resp = LoginResponse.success("jwt-token", user);

        assertThat(resp.getToken()).isEqualTo("jwt-token");
        assertThat(resp.getUser()).isNotNull();
        assertThat(resp.getUser().getUsername()).isEqualTo("admin");
        assertThat(resp.getMfaRequired()).isFalse();
        assertThat(resp.getMaskedEmail()).isNull();

        resp.setToken("new-token");
        resp.setMfaRequired(true);
        resp.setMaskedEmail("ad***@test.com");
        assertThat(resp.getToken()).isEqualTo("new-token");
        assertThat(resp.getMfaRequired()).isTrue();
        assertThat(resp.getMaskedEmail()).isEqualTo("ad***@test.com");
        assertThat(resp.toString()).isNotNull();
    }

    @Test
    @DisplayName("LoginResponse - mfaRequired() factory method")
    void loginResponse_mfaRequired() {
        LoginResponse resp = LoginResponse.mfaRequired("ad***@test.com");

        assertThat(resp.getToken()).isNull();
        assertThat(resp.getUser()).isNull();
        assertThat(resp.getMfaRequired()).isTrue();
        assertThat(resp.getMaskedEmail()).isEqualTo("ad***@test.com");
        assertThat(resp.getMessage()).isNotBlank();
    }

    @Test
    @DisplayName("LoginResponse.UserInfo - fromUser factory method")
    void loginResponseUserInfo_fromUser() {
        User user = new User("user-1", "admin", "password", "Admin", "admin@test.com", "ADMIN", true, null, null);
        LoginResponse.UserInfo info = LoginResponse.UserInfo.fromUser(user);

        assertThat(info.getId()).isEqualTo("user-1");
        assertThat(info.getUsername()).isEqualTo("admin");
        assertThat(info.getName()).isEqualTo("Admin");
        assertThat(info.getRole()).isEqualTo("ADMIN");

        info.setId("user-2");
        info.setUsername("user2");
        info.setName("User2");
        info.setRole("USER");
        assertThat(info.getId()).isEqualTo("user-2");
        assertThat(info.toString()).isNotNull();

        LoginResponse.UserInfo info2 = new LoginResponse.UserInfo();
        assertThat(info2.getId()).isNull();
    }

    @Test
    @DisplayName("MfaVerifyRequest - constructeur, getters et setters")
    void mfaVerifyRequest_gettersSetters() {
        MfaVerifyRequest req = new MfaVerifyRequest("admin", "123456");
        assertThat(req.getUsername()).isEqualTo("admin");
        assertThat(req.getCode()).isEqualTo("123456");

        req.setUsername("user");
        req.setCode("654321");
        assertThat(req.getUsername()).isEqualTo("user");
        assertThat(req.getCode()).isEqualTo("654321");

        MfaVerifyRequest req2 = new MfaVerifyRequest();
        assertThat(req2.getUsername()).isNull();
        assertThat(req.toString()).isNotNull();
        assertThat(req.hashCode()).isNotZero();
    }

    @Test
    @DisplayName("ErrorResponse - constructeur, getters et setters")
    void errorResponse_gettersSetters() {
        Instant now = Instant.now();
        ErrorResponse err = new ErrorResponse("Error msg", 400, now, "/api/test");

        assertThat(err.getMessage()).isEqualTo("Error msg");
        assertThat(err.getStatus()).isEqualTo(400);
        assertThat(err.getTimestamp()).isEqualTo(now);
        assertThat(err.getPath()).isEqualTo("/api/test");

        err.setMessage("New error");
        err.setStatus(500);
        assertThat(err.getMessage()).isEqualTo("New error");
        assertThat(err.getStatus()).isEqualTo(500);

        ErrorResponse err2 = new ErrorResponse();
        assertThat(err2.getMessage()).isNull();
        assertThat(err.toString()).isNotNull();
        assertThat(err.hashCode()).isNotZero();
    }

    @Test
    @DisplayName("PagedResidentsResponse - constructeur, getters et setters")
    void pagedResidentsResponse_gettersSetters() {
        PagedResidentsResponse resp = new PagedResidentsResponse(List.of(), 0, 1, 10L, 10);

        assertThat(resp.getResidents()).isEmpty();
        assertThat(resp.getCurrentPage()).isZero();
        assertThat(resp.getTotalPages()).isOne();
        assertThat(resp.getTotalElements()).isEqualTo(10L);
        assertThat(resp.getPageSize()).isEqualTo(10);

        resp.setCurrentPage(1);
        resp.setTotalPages(5);
        resp.setTotalElements(50L);
        resp.setPageSize(10);
        assertThat(resp.getCurrentPage()).isOne();
        assertThat(resp.getTotalPages()).isEqualTo(5);

        PagedResidentsResponse resp2 = new PagedResidentsResponse();
        assertThat(resp2.getResidents()).isNull();
        assertThat(resp.toString()).isNotNull();
    }

    @Test
    @DisplayName("ResidentsResponse - constructeur, getters et setters")
    void residentsResponse_gettersSetters() {
        ResidentsResponse resp = new ResidentsResponse(List.of());
        assertThat(resp.getData()).isEmpty();

        resp.setData(null);
        assertThat(resp.getData()).isNull();

        ResidentsResponse resp2 = new ResidentsResponse();
        assertThat(resp2.getData()).isNull();
        assertThat(resp.toString()).isNotNull();
    }

    @Test
    @DisplayName("StatisticsResponse - constructeur, getters et setters")
    void statisticsResponse_gettersSetters() {
        Map<String, Long> statut = Map.of("Propriétaire Résident", 5L);
        Map<String, Long> batiment = Map.of("A", 3L, "B", 2L);
        Map<String, Long> happixType = Map.of("resident", 4L);

        StatisticsResponse stats = new StatisticsResponse(10L, 2L, 15L, 5L, statut, batiment, 7L, 3L, 1.5, happixType);

        assertThat(stats.getTotalLots()).isEqualTo(10L);
        assertThat(stats.getTotalBatiments()).isEqualTo(2L);
        assertThat(stats.getTotalOccupants()).isEqualTo(15L);
        assertThat(stats.getTotalHappix()).isEqualTo(5L);
        assertThat(stats.getStatutCount()).isEqualTo(statut);
        assertThat(stats.getBatimentCount()).isEqualTo(batiment);
        assertThat(stats.getLotsAvecOccupants()).isEqualTo(7L);
        assertThat(stats.getLotsVides()).isEqualTo(3L);
        assertThat(stats.getMoyenneOccupants()).isEqualTo(1.5);
        assertThat(stats.getHappixByType()).isEqualTo(happixType);

        stats.setTotalLots(20L);
        stats.setTotalBatiments(3L);
        assertThat(stats.getTotalLots()).isEqualTo(20L);

        StatisticsResponse stats2 = new StatisticsResponse();
        assertThat(stats2.getTotalLots()).isZero();
        assertThat(stats.toString()).isNotNull();
    }

    @Test
    @DisplayName("ResidentHistoryResponse - from() factory method")
    void residentHistoryResponse_from() {
        Instant now = Instant.now();
        ChangeDetail cd = new ChangeDetail(ChangeDetail.CATEGORY_LOT, ChangeDetail.CHANGE_TYPE_MODIFIED, "Statut", "old", "new");

        ResidentHistory history = new ResidentHistory();
        history.setId("hist-1");
        history.setResidentId("res-1");
        history.setLotId("LOT-001");
        history.setBatiment("A");
        history.setEtage("1");
        history.setPorte("101");
        history.setActionType("UPDATE");
        history.setDescription("Test description");
        history.setChanges(List.of(cd));
        history.setChangedAt(now);
        history.setChangedBy("admin");

        ResidentHistoryResponse resp = ResidentHistoryResponse.from(history);

        assertThat(resp.getId()).isEqualTo("hist-1");
        assertThat(resp.getResidentId()).isEqualTo("res-1");
        assertThat(resp.getLotId()).isEqualTo("LOT-001");
        assertThat(resp.getBatiment()).isEqualTo("A");
        assertThat(resp.getEtage()).isEqualTo("1");
        assertThat(resp.getPorte()).isEqualTo("101");
        assertThat(resp.getActionType()).isEqualTo("UPDATE");
        assertThat(resp.getDescription()).isEqualTo("Test description");
        assertThat(resp.getChanges()).hasSize(1);
        assertThat(resp.getChangedAt()).isEqualTo(now);
        assertThat(resp.getChangedBy()).isEqualTo("admin");

        resp.setId("hist-2");
        resp.setLotId("LOT-002");
        assertThat(resp.getId()).isEqualTo("hist-2");

        ResidentHistoryResponse resp2 = new ResidentHistoryResponse();
        assertThat(resp2.getId()).isNull();
        assertThat(resp.toString()).isNotNull();
        assertThat(resp.hashCode()).isNotZero();
    }

    // ==================== equals / hashCode ====================

    @Test
    @DisplayName("LoginRequest - equals et hashCode")
    void loginRequest_equalsHashCode() {
        LoginRequest a = new LoginRequest("admin", "pass");
        LoginRequest b = new LoginRequest("admin", "pass");
        LoginRequest c = new LoginRequest("user", "pass");

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("string");
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("MfaVerifyRequest - equals et hashCode")
    void mfaVerifyRequest_equalsHashCode() {
        MfaVerifyRequest a = new MfaVerifyRequest("admin", "123456");
        MfaVerifyRequest b = new MfaVerifyRequest("admin", "123456");
        MfaVerifyRequest c = new MfaVerifyRequest("user", "123456");

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("ErrorResponse - equals et hashCode")
    void errorResponse_equalsHashCode() {
        Instant now = Instant.now();
        ErrorResponse a = new ErrorResponse("Error", 400, now, "/api");
        ErrorResponse b = new ErrorResponse("Error", 400, now, "/api");
        ErrorResponse c = new ErrorResponse("Other", 500, now, "/api");

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("PagedResidentsResponse - equals et hashCode")
    void pagedResidentsResponse_equalsHashCode() {
        PagedResidentsResponse a = new PagedResidentsResponse(List.of(), 0, 1, 10L, 10);
        PagedResidentsResponse b = new PagedResidentsResponse(List.of(), 0, 1, 10L, 10);
        PagedResidentsResponse c = new PagedResidentsResponse(List.of(), 1, 2, 20L, 10);

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("StatisticsResponse - equals et hashCode")
    void statisticsResponse_equalsHashCode() {
        StatisticsResponse a = new StatisticsResponse(10L, 2L, 15L, 5L, Map.of(), Map.of(), 7L, 3L, 1.5, Map.of());
        StatisticsResponse b = new StatisticsResponse(10L, 2L, 15L, 5L, Map.of(), Map.of(), 7L, 3L, 1.5, Map.of());
        StatisticsResponse c = new StatisticsResponse(20L, 2L, 15L, 5L, Map.of(), Map.of(), 7L, 3L, 1.5, Map.of());

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());

        // Exercise remaining setters
        a.setTotalOccupants(25L);
        a.setTotalHappix(10L);
        a.setStatutCount(Map.of("key", 1L));
        a.setBatimentCount(Map.of("A", 2L));
        a.setLotsAvecOccupants(8L);
        a.setLotsVides(2L);
        a.setMoyenneOccupants(2.5);
        a.setHappixByType(Map.of("resident", 3L));
        assertThat(a.getTotalOccupants()).isEqualTo(25L);
        assertThat(a.getTotalHappix()).isEqualTo(10L);
    }

    @Test
    @DisplayName("LoginResponse - equals, hashCode et AllArgsConstructor")
    void loginResponse_equalsHashCodeAllArgs() {
        User user = new User("user-1", "admin", "password", "Admin", "admin@test.com", "ADMIN", true, null, null);
        LoginResponse.UserInfo info = LoginResponse.UserInfo.fromUser(user);

        LoginResponse a = new LoginResponse("token", info, false, null, null);
        LoginResponse b = new LoginResponse("token", info, false, null, null);
        LoginResponse c = new LoginResponse("other", info, true, "mail", "msg");

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("string");

        a.setMessage("test");
        assertThat(a.getMessage()).isEqualTo("test");
    }

    @Test
    @DisplayName("ResidentHistoryResponse - equals")
    void residentHistoryResponse_equals() {
        ResidentHistoryResponse a = new ResidentHistoryResponse("id", "res", "lot", "A", "1", "101", "UPDATE", "desc", List.of(), Instant.now(), "admin");
        ResidentHistoryResponse b = new ResidentHistoryResponse("id", "res", "lot", "A", "1", "101", "UPDATE", "desc", List.of(), a.getChangedAt(), "admin");

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());

        // Exercise remaining setters
        a.setResidentId("res-2");
        a.setBatiment("B");
        a.setEtage("2");
        a.setPorte("202");
        a.setActionType("DELETE");
        a.setDescription("new desc");
        a.setChanges(null);
        a.setChangedAt(Instant.now());
        a.setChangedBy("user");
        assertThat(a.getResidentId()).isEqualTo("res-2");
    }

    @Test
    @DisplayName("ResidentsResponse - equals et hashCode")
    void residentsResponse_equalsHashCode() {
        ResidentsResponse a = new ResidentsResponse(List.of());
        ResidentsResponse b = new ResidentsResponse(List.of());

        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }
}
