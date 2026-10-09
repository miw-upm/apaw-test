package es.upm.miw.apaw.functionaltests.powerofattorney;

import es.upm.miw.apaw.functionaltests.powerofattorney.dto.CreationPowerOfAttorneyParty;
import es.upm.miw.apaw.functionaltests.powerofattorney.dto.PowerOfAttorneyPartyPatch;
import es.upm.miw.apaw.functionaltests.powerofattorney.model.PowerOfAttorneyParty;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import java.util.List;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = PowerOfAttorneyPartyResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class PowerOfAttorneyPartyResourceFT {
    private static final UUID PARTY_0 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff000a");
    private static final UUID PARTY_1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff000b");
    private static final UUID PARTY_2 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff000c");
    private static final UUID PARTY_3 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff000d");
    private static final UUID PARTY_4 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff000e");
    private static final UUID PARTY_5 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff000f");
    private static final UUID USER_0 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");
    private static final UUID USER_1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0001");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = PowerOfAttorneyPartyClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private PowerOfAttorneyPartyClient partyClient;

    @Test
    void testReadParty() {
        PowerOfAttorneyParty party = this.partyClient.read(PARTY_0);
        assertThat(party.getId()).isEqualTo(PARTY_0);
        assertThat(party.getAge()).isEqualTo(35);
        assertThat(party.getFullMentalCapacity()).isTrue();
        assertThat(party.getUserSnapshot().getId()).isEqualTo(USER_0);
    }

    @Test
    void testReadNotFound() {
        assertThatThrownBy(() -> this.partyClient.read(UUID.randomUUID()))
                .isInstanceOfSatisfying(FeignException.class, exception -> assertThat(exception.status()).isEqualTo(404));
    }

    @Test
    void testFindAllContainsSeededParties() {
        assertThat(this.partyClient.findAll()).extracting(PowerOfAttorneyParty::getId)
                .contains(PARTY_0, PARTY_1, PARTY_2, PARTY_3, PARTY_4, PARTY_5);
    }

    @Test
    void testFindReportContainsSeededUser() {
        assertThat(this.partyClient.findReport())
                .anySatisfy(report -> {
                    assertThat(report.getUserId()).isEqualTo("eeeeffff0000");
                    assertThat(report.getTotalPowerOfAttorneysPresent()).isPositive();
                    assertThat(report.getPrincipalCount()).isNotNegative();
                    assertThat(report.getAttorneyCount()).isNotNegative();
                });
    }

    @Test
    void testCreateAndDeleteUnreferencedParty() {
        PowerOfAttorneyParty created = this.partyClient.create(
                new CreationPowerOfAttorneyParty(40, true, null, false, USER_1));
        assertThat(created.getId()).isNotNull();
        assertThat(created.getAge()).isEqualTo(40);
        assertThat(created.getFullMentalCapacity()).isTrue();
        assertThat(created.getRepresentationCompany()).isFalse();
        assertThat(created.getUserSnapshot().getId()).isEqualTo(USER_1);
        this.partyClient.delete(created.getId());
        assertThatThrownBy(() -> this.partyClient.read(created.getId()))
                .isInstanceOfSatisfying(FeignException.class, exception -> assertThat(exception.status()).isEqualTo(404));
    }

    @Test
    void testCreateInvalidParty() {
        assertThatThrownBy(() -> this.partyClient.create(
                new CreationPowerOfAttorneyParty(null, true, null, false, USER_0)))
                .isInstanceOfSatisfying(FeignException.class, exception -> assertThat(exception.status()).isEqualTo(400));
    }

    @Test
    void testUpdateParty() {
        PowerOfAttorneyParty created = this.partyClient.create(
                new CreationPowerOfAttorneyParty(40, true, null, false, USER_1));
        PowerOfAttorneyParty updated = this.partyClient.update(created.getId(),
                new CreationPowerOfAttorneyParty(51, false, "Updated Legal S.L.", true, USER_1));
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getAge()).isEqualTo(51);
        assertThat(updated.getFullMentalCapacity()).isFalse();
        assertThat(updated.getCompanyName()).isEqualTo("Updated Legal S.L.");
        assertThat(updated.getRepresentationCompany()).isTrue();
        this.partyClient.delete(created.getId());
    }

    @Test
    void testUpdateNotFound() {
        assertThatThrownBy(() -> this.partyClient.update(UUID.randomUUID(),
                new CreationPowerOfAttorneyParty(51, true, null, false, USER_1)))
                .isInstanceOfSatisfying(FeignException.class, exception -> assertThat(exception.status()).isEqualTo(404));
    }

    @Test
    void testDeleteReferencedPartyReturnsConflict() {
        assertThatThrownBy(() -> this.partyClient.delete(PARTY_0))
                .isInstanceOfSatisfying(FeignException.class, exception -> assertThat(exception.status()).isEqualTo(409));
    }

    @Test
    void testPatchOnlyChangesProvidedFields() {
        PowerOfAttorneyParty created = this.partyClient.create(
                new CreationPowerOfAttorneyParty(40, true, null, false, USER_1));
        this.partyClient.patch(List.of(new PowerOfAttorneyPartyPatch(created.getId(), 42, null)));
        PowerOfAttorneyParty updated = this.partyClient.read(created.getId());
        assertThat(updated.getAge()).isEqualTo(42);
        assertThat(updated.getFullMentalCapacity()).isTrue();
        this.partyClient.delete(created.getId());
    }

    @Test
    void testPatchWithoutChangesReturnsBadRequest() {
        assertThatThrownBy(() -> this.partyClient.patch(List.of(new PowerOfAttorneyPartyPatch(PARTY_2, null, null))))
                .isInstanceOfSatisfying(FeignException.class, exception -> assertThat(exception.status()).isEqualTo(400));
    }
}
