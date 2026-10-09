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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = EvidenceResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class EvidenceResourceFT {
    private static final LocalDateTime COLLECTION_DATE = LocalDateTime.of(2025, 1, 1, 8, 0);
    private static final UUID CUSTODIAN_ID_0 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");
    private static final UUID CUSTODIAN_ID_1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0001");
    private static final UUID RECORD_ID_0 = UUID.fromString("cccccccc-dddd-eeee-ffff-aaaabbbb0000");
    private static final String EVIDENCE_PREFIX = "eeeeeeee-ffff-aaaa-bbbb-ccccdddd";
    // EVIDENCE_0: no confidencial, registros COLLECTED(30), TRANSFERRED(45) y ANALYZED(120)
    private static final UUID EVIDENCE_ID_0 = UUID.fromString(EVIDENCE_PREFIX + "0000");
    // EVIDENCE_1: confidencial y sin registros
    private static final UUID EVIDENCE_ID_1 = UUID.fromString(EVIDENCE_PREFIX + "0001");
    // EVIDENCE_2: no confidencial, registros INSPECTED(sin duración) y STORED(60)
    private static final UUID EVIDENCE_ID_2 = UUID.fromString(EVIDENCE_PREFIX + "0002");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = {EvidenceClient.class, CustodyRecordClient.class})
    static class ClientConfiguration {
    }

    @Autowired
    private EvidenceClient client;
    @Autowired
    private CustodyRecordClient custodyRecordClient;

    @Test
    void testCreate() {
        CustodyRecord first = this.createCustodyRecord(CUSTODIAN_ID_0);
        CustodyRecord second = this.createCustodyRecord(CUSTODIAN_ID_1);
        CreationEvidence creation = this.creation(List.of(first.getId(), second.getId()));
        creation.setDescription("Feign description");
        creation.setSource("Feign source");

        Evidence actual = this.client.create(creation);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getTitle()).isEqualTo(creation.getTitle());
        assertThat(actual.getDescription()).isEqualTo("Feign description");
        assertThat(actual.getEvidenceType()).isEqualTo(EvidenceType.DIGITAL);
        assertThat(actual.getStatus()).isEqualTo(EvidenceStatus.REGISTERED);
        assertThat(actual.getCollectionDate()).isEqualTo(COLLECTION_DATE);
        assertThat(actual.getSource()).isEqualTo("Feign source");
        assertThat(actual.getConfidential()).isFalse();
        assertThat(actual.getCustodyRecords()).extracting(CustodyRecord::getId)
                .containsExactly(first.getId(), second.getId());
        assertThat(this.client.find(EvidenceFindCriteria.builder().action(first.getAction()).build()))
                .extracting(Evidence::getId).containsExactly(actual.getId());
    }

    @Test
    void testCreateUnknownCustodyRecord() {
        CreationEvidence creation = this.creation(List.of(UUID.randomUUID()));
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateCustodyRecordAlreadyAssociated() {
        CreationEvidence creation = this.creation(List.of(RECORD_ID_0));
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateRepeatedCustodyRecordId() {
        UUID id = this.createCustodyRecord(CUSTODIAN_ID_0).getId();
        CreationEvidence creation = this.creation(List.of(id, id));
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidTitle(String title) {
        CreationEvidence creation = this.creation(List.of());
        creation.setTitle(title);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutEvidenceType() {
        CreationEvidence creation = this.creation(List.of());
        creation.setEvidenceType(null);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutCollectionDate() {
        CreationEvidence creation = this.creation(List.of());
        creation.setCollectionDate(null);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateFutureCollectionDate() {
        CreationEvidence creation = this.creation(List.of());
        creation.setCollectionDate(LocalDateTime.now().plusDays(1));
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateNullCustodyRecordId() {
        CreationEvidence creation = this.creation(Arrays.asList((UUID) null));
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testFindAllHydratesCustodians() {
        List<Evidence> evidences = this.client.find(new EvidenceFindCriteria());
        assertThat(evidences).extracting(Evidence::getId)
                .contains(EVIDENCE_ID_0, EVIDENCE_ID_1, EVIDENCE_ID_2);
        assertThat(evidences).filteredOn(evidence -> evidence.getId().equals(EVIDENCE_ID_0))
                .singleElement().satisfies(evidence -> assertThat(evidence.getCustodyRecords())
                        .extracting(CustodyRecord::getCustodian)
                        .containsExactlyInAnyOrder(this.custodian0(), this.custodian1(), this.custodian1()));
    }

    @Test
    void testFindByConfidential() {
        assertThat(this.findIds(EvidenceFindCriteria.builder().confidential(true).build()))
                .contains(EVIDENCE_ID_1).doesNotContain(EVIDENCE_ID_0, EVIDENCE_ID_2);
        assertThat(this.findIds(EvidenceFindCriteria.builder().confidential(false).build()))
                .contains(EVIDENCE_ID_0, EVIDENCE_ID_2).doesNotContain(EVIDENCE_ID_1);
    }

    @Test
    void testFindByLongCustody() {
        assertThat(this.findIds(EvidenceFindCriteria.builder().longCustody(true).build()))
                .contains(EVIDENCE_ID_0).doesNotContain(EVIDENCE_ID_1, EVIDENCE_ID_2);
        assertThat(this.findIds(EvidenceFindCriteria.builder().longCustody(false).build()))
                .contains(EVIDENCE_ID_0, EVIDENCE_ID_2).doesNotContain(EVIDENCE_ID_1);
    }

    @Test
    void testFindByActionIgnoresCase() {
        assertThat(this.findIds(EvidenceFindCriteria.builder().action("analyzed").build()))
                .contains(EVIDENCE_ID_0).doesNotContain(EVIDENCE_ID_1, EVIDENCE_ID_2);
    }

    @Test
    void testFindByCustodianFirstName() {
        Evidence ofFirst = this.createEvidence(this.createCustodyRecord(CUSTODIAN_ID_0));
        Evidence ofSecond = this.createEvidence(this.createCustodyRecord(CUSTODIAN_ID_1));

        assertThat(this.findIds(EvidenceFindCriteria.builder().custodianFirstName("  CLIENTE0 ").build()))
                .contains(ofFirst.getId()).doesNotContain(ofSecond.getId(), EVIDENCE_ID_1);
    }

    @Test
    void testFindByUnknownCustodianFirstName() {
        assertThat(this.client.find(
                EvidenceFindCriteria.builder().custodianFirstName("nobody-" + UUID.randomUUID()).build())).isEmpty();
    }

    @Test
    void testFindCombinedCriteria() {
        assertThat(this.findIds(EvidenceFindCriteria.builder().confidential(false).longCustody(true)
                .action("analyzed").custodianFirstName("cliente1").build()))
                .contains(EVIDENCE_ID_0).doesNotContain(EVIDENCE_ID_1, EVIDENCE_ID_2);
    }

    private List<UUID> findIds(EvidenceFindCriteria criteria) {
        return this.client.find(criteria).stream().map(Evidence::getId).toList();
    }

    private CreationEvidence creation(List<UUID> custodyRecordIds) {
        return CreationEvidence.builder().title("Feign evidence " + UUID.randomUUID())
                .evidenceType(EvidenceType.DIGITAL).collectionDate(COLLECTION_DATE)
                .custodyRecordIds(custodyRecordIds).build();
    }

    private CustodyRecord createCustodyRecord(UUID custodianId) {
        return this.custodyRecordClient.create(
                new CustodyRecordDto(null, "Feign action " + UUID.randomUUID(), null, null, custodianId));
    }

    private Evidence createEvidence(CustodyRecord custodyRecord) {
        return this.client.create(this.creation(List.of(custodyRecord.getId())));
    }

    private UserSnapshot custodian0() {
        return UserSnapshot.builder().id(CUSTODIAN_ID_0).mobile("600000100").firstName("cliente0").build();
    }

    private UserSnapshot custodian1() {
        return UserSnapshot.builder().id(CUSTODIAN_ID_1).mobile("600000101").firstName("cliente1").build();
    }
}