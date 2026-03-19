package com.copro.connect.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Tests de couverture des modèles")
class ModelCoverageTest {

    @Test
    @DisplayName("Resident - constructeur, getters et setters")
    void resident_gettersSetters() {
        Instant now = Instant.now();
        Occupant occ = new Occupant("Jean", "0600000000", "jean@test.com");
        HappixAccount h = new HappixAccount("Alice", "0700000000", "alice@test.com", "borne-1", "resident", "ami");

        Resident r = new Resident("id-1", "LOT-001", "A", "1", "101", "CAVE-01",
                "Propriétaire Résident", "Dupont", "0600000000", "dupont@test.com",
                List.of(occ), List.of(h), now, now);

        assertThat(r.getId()).isEqualTo("id-1");
        assertThat(r.getLotId()).isEqualTo("LOT-001");
        assertThat(r.getBatiment()).isEqualTo("A");
        assertThat(r.getEtage()).isEqualTo("1");
        assertThat(r.getPorte()).isEqualTo("101");
        assertThat(r.getCaveId()).isEqualTo("CAVE-01");
        assertThat(r.getStatutLot()).isEqualTo("Propriétaire Résident");
        assertThat(r.getProprietaireNom()).isEqualTo("Dupont");
        assertThat(r.getProprietaireMobile()).isEqualTo("0600000000");
        assertThat(r.getProprietaireEmail()).isEqualTo("dupont@test.com");
        assertThat(r.getOccupants()).hasSize(1);
        assertThat(r.getHappixAccounts()).hasSize(1);
        assertThat(r.getCreatedAt()).isEqualTo(now);
        assertThat(r.getUpdatedAt()).isEqualTo(now);

        r.setId("id-2");
        assertThat(r.getId()).isEqualTo("id-2");

        assertThat(r.toString()).isNotNull();
        assertThat(r.hashCode()).isNotZero();
        assertThat(r).isEqualTo(r);
    }

    @Test
    @DisplayName("Occupant - constructeur, getters et setters")
    void occupant_gettersSetters() {
        Occupant o = new Occupant("Jean", "0600000000", "jean@test.com");

        assertThat(o.getNom()).isEqualTo("Jean");
        assertThat(o.getMobile()).isEqualTo("0600000000");
        assertThat(o.getEmail()).isEqualTo("jean@test.com");

        o.setNom("Marie");
        o.setMobile("0700000000");
        o.setEmail("marie@test.com");

        assertThat(o.getNom()).isEqualTo("Marie");
        assertThat(o.getMobile()).isEqualTo("0700000000");
        assertThat(o.getEmail()).isEqualTo("marie@test.com");

        Occupant o2 = new Occupant();
        assertThat(o2.getNom()).isNull();
        assertThat(o.toString()).isNotNull();
        assertThat(o.hashCode()).isNotZero();
        assertThat(o).isEqualTo(o);
    }

    @Test
    @DisplayName("HappixAccount - constructeur, getters et setters")
    void happixAccount_gettersSetters() {
        HappixAccount h = new HappixAccount("Alice", "0600000000", "alice@test.com", "borne-1", "resident", "proche");

        assertThat(h.getNom()).isEqualTo("Alice");
        assertThat(h.getMobile()).isEqualTo("0600000000");
        assertThat(h.getEmail()).isEqualTo("alice@test.com");
        assertThat(h.getNomBorne()).isEqualTo("borne-1");
        assertThat(h.getType()).isEqualTo("resident");
        assertThat(h.getRelation()).isEqualTo("proche");

        h.setNom("Bob");
        h.setMobile("0700000000");
        h.setEmail("bob@test.com");
        h.setNomBorne("borne-2");
        h.setType("autorisé");
        h.setRelation("ami");

        assertThat(h.getNom()).isEqualTo("Bob");
        assertThat(h.getMobile()).isEqualTo("0700000000");
        assertThat(h.getNomBorne()).isEqualTo("borne-2");
        assertThat(h.getType()).isEqualTo("autorisé");
        assertThat(h.getRelation()).isEqualTo("ami");

        HappixAccount h2 = new HappixAccount();
        assertThat(h2.getNom()).isNull();
        assertThat(h.toString()).isNotNull();
    }

    @Test
    @DisplayName("ChangeDetail - constructeur, getters, setters et constantes")
    void changeDetail_gettersSetters() {
        ChangeDetail cd = new ChangeDetail(ChangeDetail.CATEGORY_LOT, ChangeDetail.CHANGE_TYPE_MODIFIED,
                "Statut", "old", "new");

        assertThat(cd.getCategory()).isEqualTo("LOT");
        assertThat(cd.getChangeType()).isEqualTo("MODIFIED");
        assertThat(cd.getFieldLabel()).isEqualTo("Statut");
        assertThat(cd.getOldValue()).isEqualTo("old");
        assertThat(cd.getNewValue()).isEqualTo("new");

        cd.setCategory(ChangeDetail.CATEGORY_OCCUPANT);
        cd.setChangeType(ChangeDetail.CHANGE_TYPE_ADDED);
        cd.setFieldLabel("Occupant");
        cd.setOldValue(null);
        cd.setNewValue("Jean");

        assertThat(cd.getCategory()).isEqualTo("OCCUPANT");
        assertThat(cd.getChangeType()).isEqualTo("ADDED");
        assertThat(cd.getNewValue()).isEqualTo("Jean");

        assertThat(ChangeDetail.CATEGORY_PROPRIETAIRE).isEqualTo("PROPRIETAIRE");
        assertThat(ChangeDetail.CATEGORY_HAPPIX).isEqualTo("HAPPIX");
        assertThat(ChangeDetail.CHANGE_TYPE_REMOVED).isEqualTo("REMOVED");
        assertThat(ChangeDetail.LABEL_OCCUPANT).isEqualTo("Occupant");
        assertThat(ChangeDetail.LABEL_COMPTE_HAPPIX).isEqualTo("Compte Happix");

        ChangeDetail cd2 = new ChangeDetail();
        assertThat(cd2.getCategory()).isNull();
        assertThat(cd.toString()).isNotNull();
        assertThat(cd.hashCode()).isNotZero();
    }

    @Test
    @DisplayName("ResidentHistory - constructeur, getters et setters")
    void residentHistory_gettersSetters() {
        Instant now = Instant.now();
        ChangeDetail cd = new ChangeDetail(ChangeDetail.CATEGORY_LOT, ChangeDetail.CHANGE_TYPE_MODIFIED, "Lot", "a", "b");

        ResidentHistory h = new ResidentHistory("hist-1", "res-1", "LOT-001", "A", "1", "101",
                "UPDATE", "Test desc", List.of(cd), now, "admin", "A-1-101");

        assertThat(h.getId()).isEqualTo("hist-1");
        assertThat(h.getResidentId()).isEqualTo("res-1");
        assertThat(h.getLotId()).isEqualTo("LOT-001");
        assertThat(h.getBatiment()).isEqualTo("A");
        assertThat(h.getEtage()).isEqualTo("1");
        assertThat(h.getPorte()).isEqualTo("101");
        assertThat(h.getActionType()).isEqualTo("UPDATE");
        assertThat(h.getDescription()).isEqualTo("Test desc");
        assertThat(h.getChanges()).hasSize(1);
        assertThat(h.getChangedAt()).isEqualTo(now);
        assertThat(h.getChangedBy()).isEqualTo("admin");
        assertThat(h.getApartmentKey()).isEqualTo("A-1-101");

        ResidentHistory h2 = new ResidentHistory();
        h2.setId("hist-2");
        assertThat(h2.getId()).isEqualTo("hist-2");
        assertThat(h.toString()).isNotNull();
    }

    @Test
    @DisplayName("MfaCode - constructeur, getters et setters")
    void mfaCode_gettersSetters() {
        Instant now = Instant.now();
        Instant expires = now.plusSeconds(300);

        MfaCode code = new MfaCode("mfa-1", "admin", "123456", 0, now, expires, false);

        assertThat(code.getId()).isEqualTo("mfa-1");
        assertThat(code.getUsername()).isEqualTo("admin");
        assertThat(code.getCode()).isEqualTo("123456");
        assertThat(code.getAttempts()).isZero();
        assertThat(code.getCreatedAt()).isEqualTo(now);
        assertThat(code.getExpiresAt()).isEqualTo(expires);
        assertThat(code.isUsed()).isFalse();

        code.setAttempts(3);
        code.setUsed(true);
        assertThat(code.getAttempts()).isEqualTo(3);
        assertThat(code.isUsed()).isTrue();

        MfaCode code2 = new MfaCode();
        assertThat(code2.getCode()).isNull();
        assertThat(code.toString()).isNotNull();
    }

    @Test
    @DisplayName("Resident - equals et hashCode")
    void resident_equalsHashCode() {
        Instant now = Instant.now();
        Resident a = new Resident("id-1", "LOT-001", "A", "1", "101", null, null, "Dupont", null, null, List.of(), List.of(), now, now);
        Resident b = new Resident("id-1", "LOT-001", "A", "1", "101", null, null, "Dupont", null, null, List.of(), List.of(), now, now);
        Resident c = new Resident("id-2", "LOT-002", "B", "2", "201", null, null, "Martin", null, null, List.of(), List.of(), now, now);

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("string");
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("Occupant - equals et hashCode")
    void occupant_equalsHashCode() {
        Occupant a = new Occupant("Jean", "0600000000", "jean@test.com");
        Occupant b = new Occupant("Jean", "0600000000", "jean@test.com");
        Occupant c = new Occupant("Marie", "0700000000", null);

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("HappixAccount - equals et hashCode")
    void happixAccount_equalsHashCode() {
        HappixAccount a = new HappixAccount("Alice", "0600000000", "alice@test.com", "borne-1", "resident", "ami");
        HappixAccount b = new HappixAccount("Alice", "0600000000", "alice@test.com", "borne-1", "resident", "ami");
        HappixAccount c = new HappixAccount("Bob", null, null, null, null, null);

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("ChangeDetail - equals et hashCode")
    void changeDetail_equalsHashCode() {
        ChangeDetail a = new ChangeDetail("LOT", "MODIFIED", "Statut", "old", "new");
        ChangeDetail b = new ChangeDetail("LOT", "MODIFIED", "Statut", "old", "new");
        ChangeDetail c = new ChangeDetail("OCCUPANT", "ADDED", "Occupant", null, "Jean");

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("MfaCode - equals et hashCode")
    void mfaCode_equalsHashCode() {
        Instant now = Instant.now();
        MfaCode a = new MfaCode("id", "admin", "123456", 0, now, now, false);
        MfaCode b = new MfaCode("id", "admin", "123456", 0, now, now, false);
        MfaCode c = new MfaCode("id2", "user", "654321", 3, now, now, true);

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("ResidentHistory - equals et hashCode")
    void residentHistory_equalsHashCode() {
        Instant now = Instant.now();
        ResidentHistory a = new ResidentHistory("id", "res", "lot", "A", "1", "101", "UPDATE", "desc", List.of(), now, "admin", "A-1-101");
        ResidentHistory b = new ResidentHistory("id", "res", "lot", "A", "1", "101", "UPDATE", "desc", List.of(), now, "admin", "A-1-101");
        ResidentHistory c = new ResidentHistory("id2", "res2", "lot2", "B", "2", "201", "DELETE", "desc2", List.of(), now, "user", "B-2-201");

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("User - equals et hashCode")
    void user_equalsHashCode() {
        Instant now = Instant.now();
        User a = new User("id", "admin", "pass", "Admin", "admin@test.com", "ADMIN", true, now, now);
        User b = new User("id", "admin", "pass", "Admin", "admin@test.com", "ADMIN", true, now, now);
        User c = new User("id2", "user", "pass2", "User", "user@test.com", "USER", false, now, now);

        assertThat(a).isEqualTo(b);
        assertThat(a).isNotEqualTo(c);
        assertThat(a).isNotEqualTo(null);
        assertThat(a).isNotEqualTo("string");
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
    }

    @Test
    @DisplayName("User - getters, setters et UserDetails")
    void user_gettersAndUserDetails() {
        Instant now = Instant.now();
        User user = new User("user-1", "admin", "password", "Admin", "admin@test.com", "ADMIN", true, now, now);

        assertThat(user.getId()).isEqualTo("user-1");
        assertThat(user.getUsername()).isEqualTo("admin");
        assertThat(user.getPassword()).isEqualTo("password");
        assertThat(user.getName()).isEqualTo("Admin");
        assertThat(user.getEmail()).isEqualTo("admin@test.com");
        assertThat(user.getRole()).isEqualTo("ADMIN");
        assertThat(user.isMfaEnabled()).isTrue();
        assertThat(user.getCreatedAt()).isEqualTo(now);

        assertThat(user.getAuthorities()).hasSize(1);
        assertThat(user.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.isEnabled()).isTrue();

        User userNoRole = new User();
        userNoRole.setRole(null);
        assertThat(userNoRole.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_USER");

        assertThat(user.toString()).isNotNull();
    }
}
