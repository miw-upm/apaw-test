package es.upm.miw.apaw.functionaltests.expertdirectoryservices;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = ExpertServiceScheduleResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class ExpertServiceScheduleResourceFT {
    private static final String USER_PREFIX = "aaaaaaaa-bbbb-cccc-dddd-eeeeffff";
    private static final UUID USER_ID_3 = UUID.fromString(USER_PREFIX + "0003");
    private static final UUID USER_ID_4 = UUID.fromString(USER_PREFIX + "0004");
    private static final UUID USER_ID_5 = UUID.fromString(USER_PREFIX + "0005");
    private static final String PROFILE_PREFIX = "cccccccc-dddd-eeee-ffff-aaaaaaaa";
    private static final UUID PROFILE_ID_0 = UUID.fromString(PROFILE_PREFIX + "0000");
    private static final UUID PROFILE_ID_1 = UUID.fromString(PROFILE_PREFIX + "0001");
    private static final UUID PROFILE_ID_3 = UUID.fromString(PROFILE_PREFIX + "0003");
    private static final UUID UNKNOWN_ID = UUID.fromString(PROFILE_PREFIX + "9999");
    private static final UUID SCHEDULE_ID_0 = UUID.fromString("cccccccc-dddd-eeee-ffff-cccccccc0000");
    private static final UUID SCHEDULE_ID_1 = UUID.fromString("cccccccc-dddd-eeee-ffff-cccccccc0001");
    private static final String TARIFF_CODE_0 = "EXP-TAR-0000";
    private static final String TARIFF_CODE_1 = "EXP-TAR-0001";

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = {ExpertServiceScheduleClient.class, LegalExpertProfileClient.class})
    static class ClientConfiguration {
    }

    @Autowired
    private ExpertServiceScheduleClient client;
    @Autowired
    private LegalExpertProfileClient profileClient;

    // No hay DELETE de tarifas: se usan códigos y especialidades únicos en cada ejecución.
    private LegalExpertProfile createProfile(UUID userId, String specialtyArea) {
        String suffix = UUID.randomUUID().toString();
        return this.profileClient.create(LegalExpertProfile.builder()
                .taxIdCode("FT-TAX-" + suffix)
                .specialtyArea(specialtyArea)
                .yearsOfExperience(7)
                .userSnapshot(UserSnapshot.builder().id(userId).build())
                .build());
    }

    private ExpertServiceScheduleFindCriteria minRate(String minRateAmount) {
        return ExpertServiceScheduleFindCriteria.builder().minRateAmount(new BigDecimal(minRateAmount)).build();
    }

    private CreationExpertServiceSchedule creation(List<UUID> profileIds) {
        return CreationExpertServiceSchedule.builder()
                .tariffCode("FT-TAR-" + UUID.randomUUID())
                .description("Feign tariff")
                .rateAmount(new BigDecimal("250.00"))
                .legalExpertProfileIds(profileIds)
                .build();
    }

    @Test
    void testCreate() {
        String specialtyArea = "FT law " + UUID.randomUUID();
        LegalExpertProfile first = this.createProfile(USER_ID_4, specialtyArea);
        LegalExpertProfile second = this.createProfile(USER_ID_5, specialtyArea);
        CreationExpertServiceSchedule creation = this.creation(List.of(first.getId(), second.getId()));
        creation.setSpecialCondition("Night service");

        ExpertServiceSchedule created = this.client.create(creation);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getTariffCode()).isEqualTo(creation.getTariffCode());
        assertThat(created.getDescription()).isEqualTo("Feign tariff");
        assertThat(created.getRateAmount()).isEqualByComparingTo("250.00");
        assertThat(created.getCurrency()).isEqualTo("EUR");
        assertThat(created.getSpecialCondition()).isEqualTo("Night service");
        assertThat(created.getCreationDate()).isEqualTo(LocalDate.now());
        assertThat(created.getLegalExpertProfiles()).extracting(LegalExpertProfile::getId)
                .containsExactly(first.getId(), second.getId());
        assertThat(this.client.find(ExpertServiceScheduleFindCriteria.builder().specialtyArea(specialtyArea).build()))
                .singleElement().satisfies(schedule -> {
                    assertThat(schedule.getId()).isEqualTo(created.getId());
                    assertThat(schedule.getLegalExpertProfiles()).extracting(LegalExpertProfile::getId)
                            .containsExactlyInAnyOrder(first.getId(), second.getId());
                });
    }

    @Test
    void testCreateWithExplicitCurrency() {
        CreationExpertServiceSchedule creation = this.creation(List.of());
        creation.setCurrency("USD");

        assertThat(this.client.create(creation).getCurrency()).isEqualTo("USD");
    }

    @Test
    void testCreateWithoutProfiles() {
        assertThat(this.client.create(this.creation(null)).getLegalExpertProfiles()).isEmpty();
        assertThat(this.client.create(this.creation(List.of())).getLegalExpertProfiles()).isEmpty();
    }

    @Test
    void testCreateDuplicateTariffCode() {
        CreationExpertServiceSchedule creation = this.creation(List.of());
        creation.setTariffCode(TARIFF_CODE_0);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateUnknownProfile() {
        CreationExpertServiceSchedule creation = this.creation(List.of(UNKNOWN_ID));
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateDuplicatedProfileIds() {
        UUID profileId = this.createProfile(USER_ID_4, "FT law " + UUID.randomUUID()).getId();
        CreationExpertServiceSchedule creation = this.creation(List.of(profileId, profileId));
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateProfileAssignedToSeededSchedule() {
        CreationExpertServiceSchedule creation = this.creation(List.of(PROFILE_ID_0));
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateProfileAssignedToNewSchedule() {
        UUID profileId = this.createProfile(USER_ID_4, "FT law " + UUID.randomUUID()).getId();
        this.client.create(this.creation(List.of(profileId)));
        CreationExpertServiceSchedule other = this.creation(List.of(profileId));
        assertThatThrownBy(() -> this.client.create(other)).isInstanceOf(FeignException.Conflict.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidTariffCode(String tariffCode) {
        CreationExpertServiceSchedule creation = this.creation(List.of());
        creation.setTariffCode(tariffCode);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidDescription(String description) {
        CreationExpertServiceSchedule creation = this.creation(List.of());
        creation.setDescription(description);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutRateAmount() {
        CreationExpertServiceSchedule creation = this.creation(List.of());
        creation.setRateAmount(null);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-10.50"})
    void testCreateNotPositiveRateAmount(String rateAmount) {
        CreationExpertServiceSchedule creation = this.creation(List.of());
        creation.setRateAmount(new BigDecimal(rateAmount));
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {"eur", "EURO", "E1R", ""})
    void testCreateInvalidCurrency(String currency) {
        CreationExpertServiceSchedule creation = this.creation(List.of());
        creation.setCurrency(currency);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateNullProfileId() {
        CreationExpertServiceSchedule creation = this.creation(Arrays.asList((UUID) null));
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testFindWithoutCriteria() {
        assertThat(this.client.find(new ExpertServiceScheduleFindCriteria()))
                .extracting(ExpertServiceSchedule::getId).containsSubsequence(SCHEDULE_ID_0, SCHEDULE_ID_1);
    }

    @Test
    void testFindReturnsSeededScheduleWithHydratedProfiles() {
        assertThat(this.client.find(this.minRate("200")))
                .filteredOn(schedule -> schedule.getId().equals(SCHEDULE_ID_1))
                .singleElement().satisfies(schedule -> {
                    assertThat(schedule.getTariffCode()).isEqualTo(TARIFF_CODE_1);
                    assertThat(schedule.getRateAmount()).isEqualByComparingTo("200.00");
                    assertThat(schedule.getCurrency()).isEqualTo("EUR");
                    assertThat(schedule.getSpecialCondition()).isEqualTo("Prepayment not required");
                    assertThat(schedule.getLegalExpertProfiles()).singleElement().satisfies(profile -> {
                        assertThat(profile.getId()).isEqualTo(PROFILE_ID_3);
                        assertThat(profile.getTaxIdCode()).isEqualTo("EXP-TAX-0003");
                        assertThat(profile.getSpecialtyArea()).isEqualTo("Labour law");
                        assertThat(profile.getUserSnapshot().getId()).isEqualTo(USER_ID_3);
                        assertThat(profile.getUserSnapshot().getFirstName()).isEqualTo("cliente3");
                        assertThat(profile.getUserSnapshot().getMobile()).isEqualTo("600000103");
                        assertThat(profile.getUserSnapshot().getEmail()).isEqualTo("cliente3@example.com");
                    });
                });
    }

    @Test
    void testFindByMinRateAmount() {
        assertThat(this.client.find(this.minRate("150")))
                .extracting(ExpertServiceSchedule::getId).contains(SCHEDULE_ID_1).doesNotContain(SCHEDULE_ID_0);
        assertThat(this.client.find(this.minRate("100")))
                .extracting(ExpertServiceSchedule::getId).contains(SCHEDULE_ID_0, SCHEDULE_ID_1);
    }

    @Test
    void testFindByWithSpecialCondition() {
        assertThat(this.client.find(ExpertServiceScheduleFindCriteria.builder().withSpecialCondition(true).build()))
                .extracting(ExpertServiceSchedule::getId).contains(SCHEDULE_ID_1).doesNotContain(SCHEDULE_ID_0);
        assertThat(this.client.find(ExpertServiceScheduleFindCriteria.builder().withSpecialCondition(false).build()))
                .extracting(ExpertServiceSchedule::getId).contains(SCHEDULE_ID_0).doesNotContain(SCHEDULE_ID_1);
    }

    @Test
    void testFindBySpecialtyAreaKeepsAllProfiles() {
        assertThat(this.client.find(ExpertServiceScheduleFindCriteria.builder().specialtyArea("Tax law").build()))
                .filteredOn(schedule -> schedule.getId().equals(SCHEDULE_ID_0))
                .singleElement().satisfies(schedule -> assertThat(schedule.getLegalExpertProfiles())
                        .extracting(LegalExpertProfile::getId).containsExactlyInAnyOrder(PROFILE_ID_0, PROFILE_ID_1));
        assertThat(this.client.find(ExpertServiceScheduleFindCriteria.builder().specialtyArea("Tax law").build()))
                .extracting(ExpertServiceSchedule::getId).doesNotContain(SCHEDULE_ID_1);
    }

    @Test
    void testFindByUnknownSpecialtyArea() {
        assertThat(this.client.find(ExpertServiceScheduleFindCriteria.builder()
                .specialtyArea("Unknown law " + UUID.randomUUID()).build())).isEmpty();
    }

    @Test
    void testFindByUserEmail() {
        assertThat(this.client.find(ExpertServiceScheduleFindCriteria.builder()
                .userEmail("cliente3@example.com").build()))
                .extracting(ExpertServiceSchedule::getId).contains(SCHEDULE_ID_1).doesNotContain(SCHEDULE_ID_0);
        assertThat(this.client.find(ExpertServiceScheduleFindCriteria.builder()
                .userEmail("CLIENTE1@Example.com").build()))
                .extracting(ExpertServiceSchedule::getId).contains(SCHEDULE_ID_0).doesNotContain(SCHEDULE_ID_1);
    }

    @Test
    void testFindByUnknownUserEmail() {
        assertThat(this.client.find(ExpertServiceScheduleFindCriteria.builder()
                .userEmail("nobody@example.com").build())).isEmpty();
    }

    @Test
    void testFindCombinedCriteria() {
        assertThat(this.client.find(ExpertServiceScheduleFindCriteria.builder()
                .minRateAmount(new BigDecimal("50"))
                .withSpecialCondition(false)
                .specialtyArea("Labour law")
                .userEmail("cliente0@example.com")
                .build()))
                .extracting(ExpertServiceSchedule::getId).contains(SCHEDULE_ID_0).doesNotContain(SCHEDULE_ID_1);
    }

    @Test
    void testFindCombinedCriteriaWithoutMatch() {
        assertThat(this.client.find(ExpertServiceScheduleFindCriteria.builder()
                .withSpecialCondition(true)
                .userEmail("cliente0@example.com")
                .build())).isEmpty();
    }

    @Test
    void testFindBlankStringsDoNotFilter() {
        assertThat(this.client.find(ExpertServiceScheduleFindCriteria.builder()
                .specialtyArea(" ").userEmail(" ").build()))
                .extracting(ExpertServiceSchedule::getId).contains(SCHEDULE_ID_0, SCHEDULE_ID_1);
    }
}
