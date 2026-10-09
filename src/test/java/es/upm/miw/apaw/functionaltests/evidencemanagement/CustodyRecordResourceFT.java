package es.upm.miw.apaw.functionaltests.evidencemanagement;

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

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.UUID;

import static java.time.temporal.ChronoUnit.MICROS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest(classes = CustodyRecordResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class CustodyRecordResourceFT {
    private static final UUID CUSTODIAN_ID_0 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");
    private static final UUID CUSTODIAN_ID_1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0001");
    private static final UUID UNKNOWN_USER_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff9999");
    private static final String PREFIX = "cccccccc-dddd-eeee-ffff-aaaabbbb";
    private static final UUID ID_0 = UUID.fromString(PREFIX + "0000");
    private static final UUID ID_1 = UUID.fromString(PREFIX + "0001");
    private static final UUID ID_2 = UUID.fromString(PREFIX + "0002");
    private static final UUID ID_3 = UUID.fromString(PREFIX + "0003");
    private static final UUID ID_4 = UUID.fromString(PREFIX + "0004");
    private static final UUID ID_5 = UUID.fromString(PREFIX + "0005");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = CustodyRecordClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private CustodyRecordClient client;

    @Test
    void testCreateUpdatePatchAndDelete() {
        String action = "Feign action " + UUID.randomUUID();
        CustodyRecord created = this.client.create(
                new CustodyRecordDto(30, action, "Laboratory", "Original notes", CUSTODIAN_ID_0));
        assertThat(created.getId()).isNotNull();
        assertThat(created.getRecordedAt()).isNotNull();
        assertThat(created.getDurationMinutes()).isEqualTo(30);
        assertThat(created.getAction()).isEqualTo(action);
        assertThat(created.getLocation()).isEqualTo("Laboratory");
        assertThat(created.getNotes()).isEqualTo("Original notes");
        assertThat(created.getCustodian().getId()).isEqualTo(CUSTODIAN_ID_0);
        CustodyRecord stored = this.client.read(created.getId());
        assertThat(stored).usingRecursiveComparison().ignoringFields("recordedAt").isEqualTo(created);
        assertThat(stored.getRecordedAt()).isCloseTo(created.getRecordedAt(), within(1, MICROS));

        String updatedAction = "Feign updated " + UUID.randomUUID();
        CustodyRecord updated = this.client.update(created.getId(),
                new CustodyRecordDto(null, updatedAction, null, null, CUSTODIAN_ID_1));
        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getAction()).isEqualTo(updatedAction);
        assertThat(updated.getDurationMinutes()).isNull();
        assertThat(updated.getLocation()).isNull();
        assertThat(updated.getNotes()).isNull();
        assertThat(updated.getCustodian().getId()).isEqualTo(CUSTODIAN_ID_1);

        CustodyRecord patched = this.client.patch(created.getId(),
                new CustodyRecordPatchDto(null, null, null, "Patched notes", null));
        assertThat(patched.getNotes()).isEqualTo("Patched notes");
        assertThat(patched.getAction()).isEqualTo(updatedAction);
        assertThat(patched.getCustodian().getId()).isEqualTo(CUSTODIAN_ID_1);

        this.client.delete(created.getId());
        assertThatThrownBy(() -> this.client.read(created.getId())).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testRead() {
        CustodyRecord custodyRecord = this.client.read(ID_0);
        assertThat(custodyRecord.getId()).isEqualTo(ID_0);
        assertThat(custodyRecord.getRecordedAt()).isEqualTo(LocalDateTime.of(2025, 1, 10, 9, 0));
        assertThat(custodyRecord.getDurationMinutes()).isEqualTo(30);
        assertThat(custodyRecord.getAction()).isEqualTo("COLLECTED");
        assertThat(custodyRecord.getLocation()).isEqualTo("Crime scene");
        assertThat(custodyRecord.getNotes()).isEqualTo("Collected and sealed at the crime scene");
        assertThat(custodyRecord.getCustodian().getId()).isEqualTo(CUSTODIAN_ID_0);
    }

    @Test
    void testFindAll() {
        assertThat(this.client.findAll()).extracting(CustodyRecord::getId)
                .containsSubsequence(ID_0, ID_1, ID_2, ID_3, ID_4, ID_5);
    }

    @Test
    void testFindActivityReport() {
        // CUSTODIAN_ID_1 custodia R1, R3 y R5 (225 min) repartidos en dos evidencias del seeder.
        var report = this.client.findActivityReport();
        assertThat(report).extracting(CustodianActivityReport::getTotalDurationMinutes)
                .isSortedAccordingTo(Comparator.reverseOrder());
        assertThat(report).filteredOn(item -> item.getCustodian().getId().equals(CUSTODIAN_ID_1))
                .singleElement().satisfies(item -> {
                    assertThat(item.getCustodian().getFirstName()).isEqualTo("cliente1");
                    assertThat(item.getCustodian().getMobile()).isEqualTo("600000101");
                    assertThat(item.getRecordsCount()).isGreaterThanOrEqualTo(3);
                    assertThat(item.getEvidencesCount()).isGreaterThanOrEqualTo(2);
                    assertThat(item.getTotalDurationMinutes()).isGreaterThanOrEqualTo(225);
                });
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidAction(String action) {
        CustodyRecordDto custodyRecordDto = new CustodyRecordDto(null, action, null, null, CUSTODIAN_ID_0);
        assertThatThrownBy(() -> this.client.create(custodyRecordDto)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutCustodian() {
        CustodyRecordDto custodyRecordDto = new CustodyRecordDto(null, "Feign action", null, null, null);
        assertThatThrownBy(() -> this.client.create(custodyRecordDto)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateUnknownCustodian() {
        CustodyRecordDto custodyRecordDto = new CustodyRecordDto(null, "Feign action", null, null, UNKNOWN_USER_ID);
        assertThatThrownBy(() -> this.client.create(custodyRecordDto)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testReadNotFound() {
        UUID id = UUID.randomUUID();
        assertThatThrownBy(() -> this.client.read(id)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testUpdateNotFound() {
        UUID id = UUID.randomUUID();
        CustodyRecordDto custodyRecordDto = new CustodyRecordDto(null, "Feign action", null, null, CUSTODIAN_ID_0);
        assertThatThrownBy(() -> this.client.update(id, custodyRecordDto))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testUpdateUnknownCustodian() {
        CustodyRecord created = this.client.create(
                new CustodyRecordDto(null, "Feign action " + UUID.randomUUID(), null, null, CUSTODIAN_ID_0));
        CustodyRecordDto custodyRecordDto = new CustodyRecordDto(null, "Feign action", null, null, UNKNOWN_USER_ID);
        assertThatThrownBy(() -> this.client.update(created.getId(), custodyRecordDto))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testPatchNotFound() {
        UUID id = UUID.randomUUID();
        CustodyRecordPatchDto patchDto = new CustodyRecordPatchDto(null, null, null, "Patched notes", null);
        assertThatThrownBy(() -> this.client.patch(id, patchDto)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testPatchBlankAction() {
        CustodyRecordPatchDto patchDto = new CustodyRecordPatchDto(null, " ", null, null, null);
        assertThatThrownBy(() -> this.client.patch(ID_0, patchDto)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testPatchUnknownCustodian() {
        CustodyRecordPatchDto patchDto = new CustodyRecordPatchDto(null, null, null, null, UNKNOWN_USER_ID);
        assertThatThrownBy(() -> this.client.patch(ID_0, patchDto)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testDeleteReferencedRecord() {
        assertThatThrownBy(() -> this.client.delete(ID_0)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testDeleteMissingRecord() {
        UUID id = UUID.randomUUID();
        assertThatCode(() -> this.client.delete(id)).doesNotThrowAnyException();
    }
}