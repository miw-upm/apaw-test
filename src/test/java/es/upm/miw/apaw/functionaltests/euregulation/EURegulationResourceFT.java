package es.upm.miw.apaw.functionaltests.euregulation;

import feign.FeignException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
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

@SpringBootTest(classes = EURegulationResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class EURegulationResourceFT {
    private static final String REFERENCE_NUMBER_0 = "Regulation (EU) 2016/679";
    private static final String REFERENCE_NUMBER_1 = "Regulation (EU) 2024/1689";
    private static final String REFERENCE_NUMBER_2 = "Directive (EU) 2022/2555";
    private static final UUID UNKNOWN_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff9999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = EURegulationClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private EURegulationClient client;

    @BeforeEach
    void announceFunctionalTest(TestInfo testInfo) {
        System.out.println("[euRegulation] Running functional test: " + testInfo.getDisplayName());
    }

    // Checks that the seeded EU regulations retain their expected official data.
    @Test
    void testSeederRegulationsRemainUnchanged() {
        this.assertSeededRegulation(REFERENCE_NUMBER_0, "General Data Protection Regulation",
                LegalInstrumentType.REGULATION, ApplicationArea.DATA_PROTECTION, LocalDate.of(2016, 5, 24),
                null, "https://eur-lex.europa.eu/eli/reg/2016/679/oj",
                "Regulation on the protection of natural persons with regard to personal data.");
        this.assertSeededRegulation(REFERENCE_NUMBER_1, "Artificial Intelligence Act",
                LegalInstrumentType.REGULATION, ApplicationArea.DIGITAL_TECHNOLOGY, LocalDate.of(2024, 8, 1),
                null, "https://eur-lex.europa.eu/eli/reg/2024/1689/oj",
                "Regulation laying down harmonised rules on artificial intelligence.");
        this.assertSeededRegulation(REFERENCE_NUMBER_2, "NIS2 Directive",
                LegalInstrumentType.DIRECTIVE, ApplicationArea.DIGITAL_TECHNOLOGY, LocalDate.of(2023, 1, 16),
                LocalDate.of(2024, 10, 17), "https://eur-lex.europa.eu/eli/dir/2022/2555/oj",
                "Directive on measures for a high common level of cybersecurity across the Union.");
    }

    // Verifies that reading an unknown regulation returns HTTP 404.
    @Test
    void testReadNotFound() {
        assertThatThrownBy(() -> this.client.read(UNKNOWN_ID)).isInstanceOf(FeignException.NotFound.class);
    }

    // Confirms that seeded regulations are listed in a stable order.
    @Test
    void testFindAllContainsSeederAndHasDeterministicOrder() {
        List<EURegulation> regulations = this.client.findAll();
        List<EURegulation> regulationsAgain = this.client.findAll();

        assertThat(regulations).extracting(EURegulation::getOfficialReferenceNumber)
                .contains(REFERENCE_NUMBER_0, REFERENCE_NUMBER_1, REFERENCE_NUMBER_2);
        assertThat(regulationsAgain).extracting(EURegulation::getId)
                .containsExactlyElementsOf(regulations.stream().map(EURegulation::getId).toList());
    }

    // Creates an EU regulation and checks its generated identifiers and request fields.
    @Test
    void testCreate() {
        CreationEURegulation request = this.newRegulation();

        EURegulation actual = this.client.create(request);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getSequentialId()).isNotNull();
        assertThat(actual.getEntryIntoForceDate()).isNotNull();
        assertThat(actual.getRegulationName()).isEqualTo(request.getRegulationName());
        assertThat(actual.getOfficialReferenceNumber()).isEqualTo(request.getOfficialReferenceNumber());
        assertThat(actual.getInstrumentType()).isEqualTo(request.getInstrumentType());
        assertThat(actual.getApplicationArea()).isEqualTo(request.getApplicationArea());
        assertThat(actual.getLegalStatus()).isEqualTo(request.getLegalStatus());
        assertThat(actual.getIssuingBody()).isEqualTo(request.getIssuingBody());
        assertThat(actual.getTranspositionDeadline()).isEqualTo(request.getTranspositionDeadline());
        assertThat(actual.getOfficialJournalLink()).isEqualTo(request.getOfficialJournalLink());
        assertThat(actual.getSummary()).isEqualTo(request.getSummary());
    }

    // Ensures a duplicate official reference number is rejected with HTTP 409.
    @Test
    void testCreateDuplicateOfficialReferenceNumber() {
        CreationEURegulation request = this.newRegulation();
        request.setOfficialReferenceNumber(REFERENCE_NUMBER_0);

        assertThatThrownBy(() -> this.client.create(request)).isInstanceOf(FeignException.Conflict.class);
    }

    // Replaces editable regulation fields while preserving generated identifiers and entry date.
    @Test
    void testUpdateReplacesEditableFields() {
        EURegulation original = this.createRegulation();
        CreationEURegulation replacement = this.newRegulation();
        replacement.setRegulationName("Updated " + UUID.randomUUID());

        EURegulation updated = this.client.update(original.getId(), replacement);

        assertThat(updated.getId()).isEqualTo(original.getId());
        assertThat(updated.getSequentialId()).isEqualTo(original.getSequentialId());
        assertThat(updated.getEntryIntoForceDate()).isEqualTo(original.getEntryIntoForceDate());
        assertThat(updated.getRegulationName()).isEqualTo(replacement.getRegulationName());
        assertThat(updated.getOfficialReferenceNumber()).isEqualTo(replacement.getOfficialReferenceNumber());
        assertThat(updated.getInstrumentType()).isEqualTo(replacement.getInstrumentType());
        assertThat(updated.getApplicationArea()).isEqualTo(replacement.getApplicationArea());
        assertThat(updated.getLegalStatus()).isEqualTo(replacement.getLegalStatus());
        assertThat(updated.getIssuingBody()).isEqualTo(replacement.getIssuingBody());
        assertThat(updated.getTranspositionDeadline()).isEqualTo(replacement.getTranspositionDeadline());
        assertThat(updated.getOfficialJournalLink()).isEqualTo(replacement.getOfficialJournalLink());
        assertThat(updated.getSummary()).isEqualTo(replacement.getSummary());
    }

    // Ensures an update cannot reuse another regulation's official reference number.
    @Test
    void testUpdateDuplicateOfficialReferenceNumber() {
        EURegulation original = this.createRegulation();
        CreationEURegulation update = this.newRegulation();
        update.setOfficialReferenceNumber(REFERENCE_NUMBER_0);

        assertThatThrownBy(() -> this.client.update(original.getId(), update))
                .isInstanceOf(FeignException.Conflict.class);
    }

    // Verifies that updating an unknown regulation returns HTTP 404.
    @Test
    void testUpdateNotFound() {
        assertThatThrownBy(() -> this.client.update(UNKNOWN_ID, this.newRegulation()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    // Updates one field and verifies that all omitted regulation fields remain unchanged.
    @Test
    void testPatchUpdatesOnlyPresentFields() {
        EURegulation original = this.createRegulation();
        String updatedName = "Patched " + UUID.randomUUID();

        EURegulation patched = this.client.patch(original.getId(),
                EURegulationPatch.builder().regulationName(updatedName).build());

        assertThat(patched.getRegulationName()).isEqualTo(updatedName);
        assertThat(patched.getOfficialReferenceNumber()).isEqualTo(original.getOfficialReferenceNumber());
        assertThat(patched.getInstrumentType()).isEqualTo(original.getInstrumentType());
        assertThat(patched.getSequentialId()).isEqualTo(original.getSequentialId());
        assertThat(patched.getEntryIntoForceDate()).isEqualTo(original.getEntryIntoForceDate());
        assertThat(patched.getTranspositionDeadline()).isEqualTo(original.getTranspositionDeadline());
        assertThat(patched.getOfficialJournalLink()).isEqualTo(original.getOfficialJournalLink());
        assertThat(patched.getSummary()).isEqualTo(original.getSummary());
    }

    // Ensures a patch cannot reuse another regulation's official reference number.
    @Test
    void testPatchDuplicateOfficialReferenceNumber() {
        EURegulation original = this.createRegulation();

        assertThatThrownBy(() -> this.client.patch(original.getId(),
                EURegulationPatch.builder().officialReferenceNumber(REFERENCE_NUMBER_0).build()))
                .isInstanceOf(FeignException.Conflict.class);
    }

    // Verifies that patching an unknown regulation returns HTTP 404.
    @Test
    void testPatchNotFound() {
        assertThatThrownBy(() -> this.client.patch(UNKNOWN_ID,
                EURegulationPatch.builder().regulationName("Unknown regulation").build()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    // Deletes a newly created regulation and confirms it can no longer be read.
    @Test
    void testDelete() {
        EURegulation regulation = this.createRegulation();

        this.client.delete(regulation.getId());

        assertThatThrownBy(() -> this.client.read(regulation.getId()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    // Verifies that deleting an unknown regulation returns HTTP 404.
    @Test
    void testDeleteNotFound() {
        assertThatThrownBy(() -> this.client.delete(UNKNOWN_ID)).isInstanceOf(FeignException.NotFound.class);
    }

    private void assertSeededRegulation(String officialReferenceNumber, String regulationName,
            LegalInstrumentType instrumentType, ApplicationArea applicationArea, LocalDate entryIntoForceDate,
            LocalDate transpositionDeadline, String officialJournalLink, String summary) {
        EURegulation seededRegulation = this.client.findAll().stream()
                .filter(regulation -> officialReferenceNumber.equals(regulation.getOfficialReferenceNumber()))
                .findFirst().orElseThrow();
        EURegulation actual = this.client.read(seededRegulation.getId());

        assertThat(actual.getOfficialReferenceNumber()).isEqualTo(officialReferenceNumber);
        assertThat(actual.getRegulationName()).isEqualTo(regulationName);
        assertThat(actual.getInstrumentType()).isEqualTo(instrumentType);
        assertThat(actual.getApplicationArea()).isEqualTo(applicationArea);
        assertThat(actual.getLegalStatus()).isEqualTo(LegalStatus.IN_FORCE);
        assertThat(actual.getIssuingBody()).isEqualTo(IssuingBody.EUROPEAN_PARLIAMENT);
        assertThat(actual.getEntryIntoForceDate()).isEqualTo(entryIntoForceDate);
        assertThat(actual.getTranspositionDeadline()).isEqualTo(transpositionDeadline);
        assertThat(actual.getOfficialJournalLink()).isEqualTo(officialJournalLink);
        assertThat(actual.getSummary()).isEqualTo(summary);
    }

    private EURegulation createRegulation() {
        return this.client.create(this.newRegulation());
    }

    private CreationEURegulation newRegulation() {
        return CreationEURegulation.builder()
                .regulationName("FT Regulation " + UUID.randomUUID())
                .officialReferenceNumber("FT Reference " + UUID.randomUUID())
                .instrumentType(LegalInstrumentType.REGULATION)
                .applicationArea(ApplicationArea.DATA_PROTECTION)
                .legalStatus(LegalStatus.IN_FORCE)
                .issuingBody(IssuingBody.EUROPEAN_COMMISSION)
                .transpositionDeadline(LocalDate.of(2027, 1, 1))
                .officialJournalLink("https://eur-lex.europa.eu/eli/reg/test/oj")
                .summary("Functional test regulation summary.")
                .build();
    }
}
