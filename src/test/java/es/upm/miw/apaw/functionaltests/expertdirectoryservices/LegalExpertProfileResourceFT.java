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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = LegalExpertProfileResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class LegalExpertProfileResourceFT {
    private static final String USER_PREFIX = "aaaaaaaa-bbbb-cccc-dddd-eeeeffff";
    private static final UUID USER_ID_0 = UUID.fromString(USER_PREFIX + "0000");
    private static final UUID USER_ID_1 = UUID.fromString(USER_PREFIX + "0001");
    private static final UUID USER_ID_3 = UUID.fromString(USER_PREFIX + "0003");
    private static final UUID USER_ID_4 = UUID.fromString(USER_PREFIX + "0004");
    private static final UUID USER_ID_5 = UUID.fromString(USER_PREFIX + "0005");
    private static final UUID UNKNOWN_ID = UUID.fromString(USER_PREFIX + "9999");
    private static final String PROFILE_PREFIX = "cccccccc-dddd-eeee-ffff-aaaaaaaa";
    private static final UUID PROFILE_ID_0 = UUID.fromString(PROFILE_PREFIX + "0000");
    private static final UUID PROFILE_ID_1 = UUID.fromString(PROFILE_PREFIX + "0001");
    private static final UUID PROFILE_ID_2 = UUID.fromString(PROFILE_PREFIX + "0002");
    private static final UUID PROFILE_ID_3 = UUID.fromString(PROFILE_PREFIX + "0003");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = {LegalExpertProfileClient.class, ExpertServiceScheduleClient.class})
    static class ClientConfiguration {
    }

    @Autowired
    private LegalExpertProfileClient client;
    @Autowired
    private ExpertServiceScheduleClient scheduleClient;

    private LegalExpertProfile newProfile() {
        String suffix = UUID.randomUUID().toString();
        return LegalExpertProfile.builder()
                .taxIdCode("FT-TAX-" + suffix)
                .professionalLicense("FT-LIC-" + suffix)
                .specialtyArea("FT law " + suffix)
                .yearsOfExperience(5)
                .userSnapshot(UserSnapshot.builder().id(USER_ID_4).build())
                .build();
    }

    @Test
    void testCreateReadUpdateAndDelete() {
        LegalExpertProfile profile = this.newProfile();
        LegalExpertProfile created = this.client.create(profile);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getTaxIdCode()).isEqualTo(profile.getTaxIdCode());
        assertThat(created.getProfessionalLicense()).isEqualTo(profile.getProfessionalLicense());
        assertThat(created.getSpecialtyArea()).isEqualTo(profile.getSpecialtyArea());
        assertThat(created.getYearsOfExperience()).isEqualTo(5);
        assertThat(created.getRequiresPrepayment()).isFalse();
        assertThat(created.getPartnershipDate()).isEqualTo(LocalDate.now());
        assertThat(created.getUserSnapshot().getId()).isEqualTo(USER_ID_4);

        LegalExpertProfile read = this.client.read(created.getId().toString());
        assertThat(read.getId()).isEqualTo(created.getId());
        assertThat(read.getTaxIdCode()).isEqualTo(profile.getTaxIdCode());

        LegalExpertProfile update = LegalExpertProfile.builder()
                .taxIdCode(profile.getTaxIdCode() + "-U")
                .professionalLicense(profile.getProfessionalLicense() + "-U")
                .specialtyArea("Updated area")
                .yearsOfExperience(15)
                .requiresPrepayment(true)
                .userSnapshot(UserSnapshot.builder().id(USER_ID_5).build())
                .build();
        LegalExpertProfile updated = this.client.update(created.getId().toString(), update);
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getTaxIdCode()).isEqualTo(update.getTaxIdCode());
        assertThat(updated.getSpecialtyArea()).isEqualTo("Updated area");
        assertThat(updated.getYearsOfExperience()).isEqualTo(15);
        assertThat(updated.getRequiresPrepayment()).isTrue();
        assertThat(updated.getPartnershipDate()).isEqualTo(created.getPartnershipDate());
        assertThat(updated.getUserSnapshot().getId()).isEqualTo(USER_ID_5);
        assertThat(this.client.read(created.getId().toString()).getSpecialtyArea()).isEqualTo("Updated area");

        assertThat(this.client.delete(created.getId().toString())).containsKey("message");
        assertThatThrownBy(() -> this.client.read(created.getId().toString()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateWithoutOptionalFields() {
        LegalExpertProfile profile = this.newProfile();
        profile.setProfessionalLicense(null);

        LegalExpertProfile created = this.client.create(profile);

        assertThat(created.getProfessionalLicense()).isNull();
        assertThat(created.getRequiresPrepayment()).isFalse();
    }

    @Test
    void testRead() {
        LegalExpertProfile profile = this.client.read(PROFILE_ID_0.toString());
        assertThat(profile.getId()).isEqualTo(PROFILE_ID_0);
        assertThat(profile.getTaxIdCode()).isEqualTo("EXP-TAX-0000");
        assertThat(profile.getProfessionalLicense()).isEqualTo("EXP-LIC-0000");
        assertThat(profile.getSpecialtyArea()).isEqualTo("Labour law");
        assertThat(profile.getYearsOfExperience()).isEqualTo(12);
        assertThat(profile.getUserSnapshot().getId()).isEqualTo(USER_ID_0);
    }

    @Test
    void testReadNotFound() {
        assertThatThrownBy(() -> this.client.read(UNKNOWN_ID.toString()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testReadInvalidId() {
        assertThatThrownBy(() -> this.client.read("not-a-uuid"))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testFindAll() {
        assertThat(this.client.findAll()).extracting(LegalExpertProfile::getId)
                .containsSubsequence(PROFILE_ID_0, PROFILE_ID_1, PROFILE_ID_2, PROFILE_ID_3);
    }

    @Test
    void testCreateDuplicateTaxIdCode() {
        LegalExpertProfile profile = this.newProfile();
        profile.setTaxIdCode("EXP-TAX-0000");
        assertThatThrownBy(() -> this.client.create(profile)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateDuplicateProfessionalLicense() {
        LegalExpertProfile profile = this.newProfile();
        profile.setProfessionalLicense("EXP-LIC-0000");
        assertThatThrownBy(() -> this.client.create(profile)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateUnknownUser() {
        LegalExpertProfile profile = this.newProfile();
        profile.setUserSnapshot(UserSnapshot.builder().id(UNKNOWN_ID).build());
        assertThatThrownBy(() -> this.client.create(profile)).isInstanceOf(FeignException.NotFound.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidTaxIdCode(String taxIdCode) {
        LegalExpertProfile profile = this.newProfile();
        profile.setTaxIdCode(taxIdCode);
        assertThatThrownBy(() -> this.client.create(profile)).isInstanceOf(FeignException.BadRequest.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidSpecialtyArea(String specialtyArea) {
        LegalExpertProfile profile = this.newProfile();
        profile.setSpecialtyArea(specialtyArea);
        assertThatThrownBy(() -> this.client.create(profile)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutYearsOfExperience() {
        LegalExpertProfile profile = this.newProfile();
        profile.setYearsOfExperience(null);
        assertThatThrownBy(() -> this.client.create(profile)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutUser() {
        LegalExpertProfile profile = this.newProfile();
        profile.setUserSnapshot(null);
        assertThatThrownBy(() -> this.client.create(profile)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testUpdateNotFound() {
        assertThatThrownBy(() -> this.client.update(UNKNOWN_ID.toString(), this.newProfile()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testUpdateInvalidId() {
        assertThatThrownBy(() -> this.client.update("not-a-uuid", this.newProfile()))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testUpdateDuplicateTaxIdCode() {
        LegalExpertProfile created = this.client.create(this.newProfile());
        LegalExpertProfile update = this.newProfile();
        update.setTaxIdCode("EXP-TAX-0001");
        assertThatThrownBy(() -> this.client.update(created.getId().toString(), update))
                .isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testUpdateDuplicateProfessionalLicense() {
        LegalExpertProfile created = this.client.create(this.newProfile());
        LegalExpertProfile update = this.newProfile();
        update.setProfessionalLicense("EXP-LIC-0001");
        assertThatThrownBy(() -> this.client.update(created.getId().toString(), update))
                .isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testUpdateUnknownUser() {
        LegalExpertProfile created = this.client.create(this.newProfile());
        LegalExpertProfile update = this.newProfile();
        update.setUserSnapshot(UserSnapshot.builder().id(UNKNOWN_ID).build());
        assertThatThrownBy(() -> this.client.update(created.getId().toString(), update))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testUpdatePartial() {
        LegalExpertProfile first = this.client.create(this.newProfile());
        LegalExpertProfile second = this.client.create(this.newProfile());

        this.client.updatePartial(List.of(
                LegalExpertProfile.builder().id(first.getId()).yearsOfExperience(25).build(),
                LegalExpertProfile.builder().id(second.getId()).specialtyArea("Patched area")
                        .userSnapshot(UserSnapshot.builder().id(USER_ID_5).build()).build()));

        LegalExpertProfile readFirst = this.client.read(first.getId().toString());
        assertThat(readFirst.getYearsOfExperience()).isEqualTo(25);
        assertThat(readFirst.getSpecialtyArea()).isEqualTo(first.getSpecialtyArea());
        assertThat(readFirst.getTaxIdCode()).isEqualTo(first.getTaxIdCode());
        LegalExpertProfile readSecond = this.client.read(second.getId().toString());
        assertThat(readSecond.getSpecialtyArea()).isEqualTo("Patched area");
        assertThat(readSecond.getYearsOfExperience()).isEqualTo(second.getYearsOfExperience());
        assertThat(readSecond.getUserSnapshot().getId()).isEqualTo(USER_ID_5);
    }

    @Test
    void testUpdatePartialDuplicatedIds() {
        LegalExpertProfile created = this.client.create(this.newProfile());
        List<LegalExpertProfile> updates = List.of(
                LegalExpertProfile.builder().id(created.getId()).yearsOfExperience(1).build(),
                LegalExpertProfile.builder().id(created.getId()).yearsOfExperience(2).build());
        assertThatThrownBy(() -> this.client.updatePartial(updates)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testUpdatePartialNotFound() {
        List<LegalExpertProfile> updates = List.of(
                LegalExpertProfile.builder().id(UNKNOWN_ID).yearsOfExperience(1).build());
        assertThatThrownBy(() -> this.client.updatePartial(updates)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testUpdatePartialDuplicateTaxIdCode() {
        LegalExpertProfile created = this.client.create(this.newProfile());
        List<LegalExpertProfile> updates = List.of(
                LegalExpertProfile.builder().id(created.getId()).taxIdCode("EXP-TAX-0000").build());
        assertThatThrownBy(() -> this.client.updatePartial(updates)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testDeleteIsIdempotent() {
        LegalExpertProfile created = this.client.create(this.newProfile());
        this.client.delete(created.getId().toString());
        assertThat(this.client.delete(created.getId().toString())).containsKey("message");
    }

    @Test
    void testDeleteInvalidId() {
        assertThatThrownBy(() -> this.client.delete("not-a-uuid")).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testDeleteProfileAssignedToSchedule() {
        assertThatThrownBy(() -> this.client.delete(PROFILE_ID_0.toString()))
                .isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testDeleteProfileAssignedToNewSchedule() {
        LegalExpertProfile created = this.client.create(this.newProfile());
        this.scheduleClient.create(CreationExpertServiceSchedule.builder()
                .tariffCode("FT-TAR-" + UUID.randomUUID()).description("Profile delete guard")
                .rateAmount(new BigDecimal("90.00")).legalExpertProfileIds(List.of(created.getId())).build());

        assertThatThrownBy(() -> this.client.delete(created.getId().toString()))
                .isInstanceOf(FeignException.Conflict.class);
        assertThat(this.client.read(created.getId().toString()).getId()).isEqualTo(created.getId());
    }

    @Test
    void testFindSpecialtyReportOfSeededData() {
        List<LegalExpertProfileSpecialtyReport> report = this.client.findSpecialtyReport();

        assertThat(report).extracting(LegalExpertProfileSpecialtyReport::getTotalSchedules)
                .isSortedAccordingTo(Comparator.reverseOrder());
        assertThat(report).filteredOn(item -> item.getSpecialtyArea().equals("Labour law"))
                .singleElement().satisfies(item -> {
                    assertThat(item.getTotalProfiles()).isGreaterThanOrEqualTo(2);
                    assertThat(item.getTotalSchedules()).isGreaterThanOrEqualTo(2);
                    assertThat(item.getAverageRateAmount()).isBetween(100.0, 200.0);
                    assertThat(item.getAverageYearsOfExperience()).isBetween(12.0, 20.0);
                    assertThat(item.getMostVeteranExpert().getId()).isEqualTo(USER_ID_3);
                    assertThat(item.getMostVeteranExpert().getFirstName()).isEqualTo("cliente3");
                    assertThat(item.getMostVeteranExpert().getMobile()).isEqualTo("600000103");
                    assertThat(item.getMostVeteranExpert().getEmail()).isEqualTo("cliente3@example.com");
                });
        assertThat(report).filteredOn(item -> item.getSpecialtyArea().equals("Tax law"))
                .singleElement().satisfies(item -> {
                    assertThat(item.getTotalProfiles()).isGreaterThanOrEqualTo(1);
                    assertThat(item.getMostVeteranExpert().getId()).isEqualTo(USER_ID_1);
                    assertThat(item.getMostVeteranExpert().getFirstName()).isEqualTo("cliente1");
                });
    }

    @Test
    void testFindSpecialtyReportExcludesProfilesWithoutSchedule() {
        LegalExpertProfile created = this.client.create(this.newProfile());

        assertThat(this.client.findSpecialtyReport()).extracting(LegalExpertProfileSpecialtyReport::getSpecialtyArea)
                .doesNotContain(created.getSpecialtyArea(), "Real estate law");
    }

    @Test
    void testFindSpecialtyReportIncludesNewSchedule() {
        LegalExpertProfile veteran = this.newProfile();
        veteran.setYearsOfExperience(30);
        LegalExpertProfile first = this.client.create(veteran);
        LegalExpertProfile junior = this.newProfile();
        junior.setSpecialtyArea(first.getSpecialtyArea());
        junior.setUserSnapshot(UserSnapshot.builder().id(USER_ID_5).build());
        LegalExpertProfile second = this.client.create(junior);
        this.scheduleClient.create(CreationExpertServiceSchedule.builder()
                .tariffCode("FT-TAR-" + UUID.randomUUID()).description("Report schedule")
                .rateAmount(new BigDecimal("120.00"))
                .legalExpertProfileIds(List.of(first.getId(), second.getId())).build());

        assertThat(this.client.findSpecialtyReport())
                .filteredOn(item -> item.getSpecialtyArea().equals(first.getSpecialtyArea()))
                .singleElement().satisfies(item -> {
                    assertThat(item.getTotalProfiles()).isEqualTo(2);
                    assertThat(item.getTotalSchedules()).isEqualTo(1);
                    assertThat(item.getAverageRateAmount()).isEqualTo(120.0);
                    assertThat(item.getAverageYearsOfExperience()).isEqualTo(17.5);
                    assertThat(item.getMostVeteranExpert().getId()).isEqualTo(USER_ID_4);
                    assertThat(item.getMostVeteranExpert().getFirstName()).isEqualTo("cliente4");
                });
    }
}
