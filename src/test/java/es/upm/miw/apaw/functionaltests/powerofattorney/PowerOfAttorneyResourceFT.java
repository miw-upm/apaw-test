package es.upm.miw.apaw.functionaltests.powerofattorney;

import es.upm.miw.apaw.functionaltests.powerofattorney.dto.CreationPowerOfAttorney;
import es.upm.miw.apaw.functionaltests.powerofattorney.dto.PowerOfAttorneyFindCriteria;
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
import java.util.List;
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
    private static final String SEEDED_IDENTITY = "00000000T";

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

    @Test
    void testFindWithoutCriteriaReturnsPowers() {
        List<PowerOfAttorney> powers = this.powerOfAttorneyClient.find(new PowerOfAttorneyFindCriteria());

        assertThat(powers).isNotEmpty();
        assertThat(powers).allSatisfy(power -> {
            assertThat(power.getId()).isNotNull();
            assertThat(power.getProtocolNumber()).isNotBlank();
            assertThat(power.getPrincipal()).isNotNull();
            assertThat(power.getAttorney()).isNotNull();
        });
    }

    @Test
    void testFindByFullMentalCapacityTrueRequiresBothPartiesToHaveCapacity() {
        List<PowerOfAttorney> powers = this.powerOfAttorneyClient.find(
                new PowerOfAttorneyFindCriteria(null, true, null, null));

        assertThat(powers).allSatisfy(power -> {
            assertThat(power.getPrincipal().getFullMentalCapacity()).isTrue();
            assertThat(power.getAttorney().getFullMentalCapacity()).isTrue();
        });
    }

    @Test
    void testFindByFullMentalCapacityFalseRequiresAtLeastOnePartyWithoutCapacity() {
        List<PowerOfAttorney> powers = this.powerOfAttorneyClient.find(
                new PowerOfAttorneyFindCriteria(null, false, null, null));

        assertThat(powers).allSatisfy(power -> assertThat(
                Boolean.FALSE.equals(power.getPrincipal().getFullMentalCapacity())
                        || Boolean.FALSE.equals(power.getAttorney().getFullMentalCapacity())).isTrue());
    }

    @Test
    void testFindByIdentityMatchesEitherPartyIgnoringCase() {
        List<PowerOfAttorney> powers = this.powerOfAttorneyClient.find(
                new PowerOfAttorneyFindCriteria(null, null, null, SEEDED_IDENTITY.toLowerCase()));

        assertThat(powers).isNotEmpty();
        assertThat(powers).allSatisfy(power -> {
            assertThat((power.getPrincipal().getUserSnapshot() != null
                    && SEEDED_IDENTITY.equalsIgnoreCase(power.getPrincipal().getUserSnapshot().getIdentity()))
                    || (power.getAttorney().getUserSnapshot() != null
                    && SEEDED_IDENTITY.equalsIgnoreCase(power.getAttorney().getUserSnapshot().getIdentity()))).isTrue();
        });
    }

    @Test
    void testFindByUnknownIdentityReturnsNoPowers() {
        List<PowerOfAttorney> powers = this.powerOfAttorneyClient.find(
                new PowerOfAttorneyFindCriteria(null, null, null, "NO-SUCH-IDENTITY-" + UUID.randomUUID()));

        assertThat(powers).isEmpty();
    }

    @Test
    void testFindByLegalPowerOfAttorneyTrueReturnsOnlyLegalPowers() {
        List<PowerOfAttorney> powers = this.powerOfAttorneyClient.find(
                new PowerOfAttorneyFindCriteria(null, null, true, null));

        assertThat(powers).allSatisfy(power -> {
            assertThat(power.getPrincipal().getAge()).isGreaterThan(18);
            assertThat(power.getPrincipal().getFullMentalCapacity()).isTrue();
            assertThat(power.getPrincipal().getUserSnapshot().getIdentity()).isNotBlank();
            assertThat(power.getAttorney().getAge()).isGreaterThan(18);
            assertThat(power.getAttorney().getFullMentalCapacity()).isTrue();
            assertThat(power.getAttorney().getUserSnapshot().getIdentity()).isNotBlank();
        });
    }

    @Test
    void testFindByLegalPowerOfAttorneyFalseReturnsOnlyNonLegalPowers() {
        List<PowerOfAttorney> powers = this.powerOfAttorneyClient.find(
                new PowerOfAttorneyFindCriteria(null, null, false, null));

        assertThat(powers).allSatisfy(power -> assertThat(
                power.getPrincipal().getAge() == null || power.getPrincipal().getAge() <= 18
                        || !Boolean.TRUE.equals(power.getPrincipal().getFullMentalCapacity())
                        || power.getPrincipal().getUserSnapshot() == null
                        || power.getPrincipal().getUserSnapshot().getIdentity() == null
                        || power.getPrincipal().getUserSnapshot().getIdentity().isBlank()
                        || power.getAttorney().getAge() == null || power.getAttorney().getAge() <= 18
                        || !Boolean.TRUE.equals(power.getAttorney().getFullMentalCapacity())
                        || power.getAttorney().getUserSnapshot() == null
                        || power.getAttorney().getUserSnapshot().getIdentity() == null
                        || power.getAttorney().getUserSnapshot().getIdentity().isBlank()).isTrue());
    }

    @Test
    void testFindCombinesStatusAndIdentity() {
        List<PowerOfAttorney> powers = this.powerOfAttorneyClient.find(
                new PowerOfAttorneyFindCriteria(PowerOfAttorneyStatus.ACTIVE, null, null, SEEDED_IDENTITY));

        assertThat(powers).isNotEmpty();
        assertThat(powers).allSatisfy(power -> {
            assertThat(power.getStatus()).isEqualTo(PowerOfAttorneyStatus.ACTIVE);
            assertThat((power.getPrincipal().getUserSnapshot() != null
                    && SEEDED_IDENTITY.equalsIgnoreCase(power.getPrincipal().getUserSnapshot().getIdentity()))
                    || (power.getAttorney().getUserSnapshot() != null
                    && SEEDED_IDENTITY.equalsIgnoreCase(power.getAttorney().getUserSnapshot().getIdentity()))).isTrue();
        });
    }
}
