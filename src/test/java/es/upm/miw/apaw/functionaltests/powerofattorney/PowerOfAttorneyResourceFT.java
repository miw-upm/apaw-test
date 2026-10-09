package es.upm.miw.apaw.functionaltests.powerofattorney;

import es.upm.miw.apaw.functionaltests.powerofattorney.dto.CreationPowerOfAttorney;
import es.upm.miw.apaw.functionaltests.powerofattorney.model.PowerOfAttorney;
import es.upm.miw.apaw.functionaltests.powerofattorney.model.PowerOfAttorneyStatus;
import es.upm.miw.apaw.functionaltests.powerofattorney.model.PowerOfAttorneyType;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalDate;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = PowerOfAttorneyResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class PowerOfAttorneyResourceFT {
    private static final UUID PARTY_0 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff000a");
    private static final UUID PARTY_1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff000b");
    private static final UUID PARTY_2 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff000c");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = PowerOfAttorneyClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private PowerOfAttorneyClient powerOfAttorneyClient;

    @Test
    void testCreateUsesDefaultTypeAndStatus() {
        String protocol = "FT-POA-" + UUID.randomUUID();
        PowerOfAttorney created = this.powerOfAttorneyClient.create(new CreationPowerOfAttorney(
                protocol, LocalDate.of(2026, 10, 1), LocalDate.of(2027, 10, 1),
                "General legal representation", null, "Functional Test Notary",
                "Functional Test Notary Office", null, null, null, PARTY_0, PARTY_2));

        assertThat(created.getId()).isNotNull();
        assertThat(created.getProtocolNumber()).isEqualTo(protocol);
        assertThat(created.getPrincipal().getId()).isEqualTo(PARTY_0);
        assertThat(created.getAttorney().getId()).isEqualTo(PARTY_2);
        assertThat(created.getType()).isEqualTo(PowerOfAttorneyType.GENERAL);
        assertThat(created.getStatus()).isEqualTo(PowerOfAttorneyStatus.ACTIVE);
    }

    @Test
    void testCreateWithoutExpirationDate() {
        String protocol = "FT-POA-NULL-EXPIRATION-" + UUID.randomUUID();
        PowerOfAttorney created = this.powerOfAttorneyClient.create(new CreationPowerOfAttorney(
                protocol, LocalDate.of(2026, 10, 1), null,
                "Representation without expiration", null, "Functional Test Notary",
                "Functional Test Notary Office", null, null, null, PARTY_0, PARTY_2));

        assertThat(created.getProtocolNumber()).isEqualTo(protocol);
        assertThat(created.getExpirationDate()).isNull();
    }

    @Test
    void testCreateInvalidWhenRequiredFieldIsMissing() {
        assertThatThrownBy(() -> this.powerOfAttorneyClient.create(new CreationPowerOfAttorney(
                "FT-POA-INVALID-" + UUID.randomUUID(), LocalDate.of(2026, 10, 1),
                LocalDate.of(2027, 10, 1), null, null, "Notary", "Office", null, null, null, PARTY_0, PARTY_2)))
                .isInstanceOfSatisfying(FeignException.class, exception -> assertThat(exception.status()).isEqualTo(400));
    }

    @Test
    void testCreateDuplicateProtocolNumberReturnsConflict() {
        assertThatThrownBy(() -> this.powerOfAttorneyClient.create(new CreationPowerOfAttorney(
                "DEV-POA-0000", LocalDate.of(2026, 10, 1), LocalDate.of(2027, 10, 1),
                "General legal representation", null, "Notary", "Office", null, null, null, PARTY_0, PARTY_2)))
                .isInstanceOfSatisfying(FeignException.class, exception -> assertThat(exception.status()).isEqualTo(409));
    }

    @Test
    void testCreateSamePrincipalAndAttorneyUser() {
        assertThatThrownBy(() -> this.powerOfAttorneyClient.create(new CreationPowerOfAttorney(
                "FT-POA-SAME-USER-" + UUID.randomUUID(), LocalDate.of(2026, 10, 1), LocalDate.of(2027, 10, 1),
                "General legal representation", null, "Notary", "Office", null, null, null, PARTY_0, PARTY_1)))
                .isInstanceOfSatisfying(FeignException.class, exception -> assertThat(exception.status()).isEqualTo(400));
    }

    @Test
    void testCreateRejectsGrantDateAfterExpirationDate() {
        assertThatThrownBy(() -> this.powerOfAttorneyClient.create(new CreationPowerOfAttorney(
                "FT-POA-INVALID-DATES-" + UUID.randomUUID(), LocalDate.of(2027, 10, 1), LocalDate.of(2026, 10, 1),
                "General legal representation", null, "Notary", "Office", null, null, null, PARTY_0, PARTY_2)))
                .isInstanceOfSatisfying(FeignException.class, exception -> assertThat(exception.status()).isEqualTo(400));
    }
}
