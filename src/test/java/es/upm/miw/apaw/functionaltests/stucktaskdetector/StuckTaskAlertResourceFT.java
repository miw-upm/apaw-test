package es.upm.miw.apaw.functionaltests.stucktaskdetector;

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

@SpringBootTest(classes = StuckTaskAlertResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class StuckTaskAlertResourceFT {
    private static final UUID RULE_ID_0 = UUID.fromString("55555555-6666-7777-8888-9999aaaa0000");
    private static final UUID ALERT_ID_0 = UUID.fromString("66666666-7777-8888-9999-aaaabbbb0000");
    private static final UUID ALERT_ID_1 = UUID.fromString("66666666-7777-8888-9999-aaaabbbb0001");
    private static final UUID ALERT_ID_2 = UUID.fromString("66666666-7777-8888-9999-aaaabbbb0002");
    private static final UUID ALERT_ID_3 = UUID.fromString("66666666-7777-8888-9999-aaaabbbb0003");
    private static final UUID ALERT_ID_4 = UUID.fromString("66666666-7777-8888-9999-aaaabbbb0004");
    private static final UUID UNKNOWN_ID = UUID.fromString("66666666-7777-8888-9999-aaaabbbb9999");
    private static final String EMAIL_0 = "cliente0@example.com";
    private static final String EMAIL_1 = "cliente1@example.com";

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = StuckTaskAlertClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private StuckTaskAlertClient client;

    private StuckTaskAlertCreation creation() {
        return StuckTaskAlertCreation.builder()
                .reference("FT-" + UUID.randomUUID()).stuckTaskRuleId(RULE_ID_0).build();
    }

    private List<UUID> findIds(StuckTaskAlertFindCriteria criteria) {
        return this.client.find(criteria).stream().map(StuckTaskAlert::getId).toList();
    }

    @Test
    void testCreateUpdatePatchAndDelete() {
        StuckTaskAlertCreation creation = this.creation();
        StuckTaskAlert alert = this.client.create(creation);
        assertThat(alert.getId()).isNotNull();
        assertThat(alert.getReference()).isEqualTo(creation.getReference());
        assertThat(alert.getDetectedAt()).isEqualTo(LocalDate.now());
        assertThat(alert.getEscalated()).isFalse();
        assertThat(alert.getResolvedAt()).isNull();
        assertThat(alert.getStuckTaskRule().getId()).isEqualTo(RULE_ID_0);

        StuckTaskAlert replacement = StuckTaskAlert.builder().reference(creation.getReference())
                .resolvedAt(LocalDate.of(2026, 2, 1)).escalated(true).build();
        StuckTaskAlert updated = this.client.update(alert.getId(), replacement);
        assertThat(updated.getResolvedAt()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(updated.getEscalated()).isTrue();
        assertThat(updated.getDetectedAt()).isEqualTo(alert.getDetectedAt());
        assertThat(updated.getStuckTaskRule().getId()).isEqualTo(RULE_ID_0);

        StuckTaskAlert patched = this.client.patch(alert.getId(),
                StuckTaskAlertPatch.builder().resolutionNotes("Patched by FT").build());
        assertThat(patched.getResolutionNotes()).isEqualTo("Patched by FT");
        assertThat(patched.getEscalated()).isTrue();
        assertThat(patched.getReference()).isEqualTo(creation.getReference());
        assertThat(this.client.read(alert.getId()).getResolutionNotes()).isEqualTo("Patched by FT");

        this.client.delete(alert.getId());
        assertThatThrownBy(() -> this.client.read(alert.getId())).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testRead() {
        StuckTaskAlert alert = this.client.read(ALERT_ID_0);
        assertThat(alert.getId()).isEqualTo(ALERT_ID_0);
        assertThat(alert.getReference()).isEqualTo("STA-2025-0001");
        assertThat(alert.getDetectedAt()).isEqualTo(LocalDate.of(2025, 2, 1));
        assertThat(alert.getResolvedAt()).isEqualTo(LocalDate.of(2025, 2, 10));
        assertThat(alert.getEscalated()).isFalse();
        assertThat(alert.getStuckTaskRule().getId()).isEqualTo(RULE_ID_0);
        assertThat(alert.getStuckTaskRule().getName()).isEqualTo("Tax procedure inactivity");
    }

    @Test
    void testFindAll() {
        assertThat(this.client.findAll()).extracting(StuckTaskAlert::getId)
                .containsSubsequence(ALERT_ID_0, ALERT_ID_1, ALERT_ID_2, ALERT_ID_3, ALERT_ID_4);
    }

    @Test
    void testCreateDuplicateReference() {
        StuckTaskAlertCreation creation = this.creation();
        creation.setReference("STA-2025-0001");
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateUnknownRule() {
        StuckTaskAlertCreation creation = this.creation();
        creation.setStuckTaskRuleId(UNKNOWN_ID);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateWithoutRule() {
        StuckTaskAlertCreation creation = this.creation();
        creation.setStuckTaskRuleId(null);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testReadNotFound() {
        assertThatThrownBy(() -> this.client.read(UNKNOWN_ID)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testUpdateNotFound() {
        StuckTaskAlert alert = StuckTaskAlert.builder().escalated(true).build();
        assertThatThrownBy(() -> this.client.update(UNKNOWN_ID, alert)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testUpdateDuplicateReference() {
        StuckTaskAlert alert = StuckTaskAlert.builder().reference("STA-2025-0001").escalated(true).build();
        assertThatThrownBy(() -> this.client.update(ALERT_ID_1, alert)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testPatchNotFound() {
        StuckTaskAlertPatch patch = StuckTaskAlertPatch.builder().escalated(true).build();
        assertThatThrownBy(() -> this.client.patch(UNKNOWN_ID, patch)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testPatchDuplicateReference() {
        StuckTaskAlertPatch patch = StuckTaskAlertPatch.builder().reference("STA-2025-0001").build();
        assertThatThrownBy(() -> this.client.patch(ALERT_ID_1, patch)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testFindWithoutCriteria() {
        assertThat(this.findIds(new StuckTaskAlertFindCriteria()))
                .contains(ALERT_ID_0, ALERT_ID_1, ALERT_ID_2, ALERT_ID_3, ALERT_ID_4);
    }

    @Test
    void testFindByProcedureKeyword() {
        assertThat(this.findIds(StuckTaskAlertFindCriteria.builder().procedureKeyword("eviction").build()))
                .contains(ALERT_ID_2, ALERT_ID_3).doesNotContain(ALERT_ID_0, ALERT_ID_1, ALERT_ID_4);
    }

    @Test
    void testFindByWithPenalty() {
        assertThat(this.findIds(StuckTaskAlertFindCriteria.builder().withPenalty(true).build()))
                .contains(ALERT_ID_0, ALERT_ID_1, ALERT_ID_4).doesNotContain(ALERT_ID_2, ALERT_ID_3);
        assertThat(this.findIds(StuckTaskAlertFindCriteria.builder().withPenalty(false).build()))
                .contains(ALERT_ID_2, ALERT_ID_3).doesNotContain(ALERT_ID_0, ALERT_ID_1, ALERT_ID_4);
    }

    @Test
    void testFindByEscalated() {
        assertThat(this.findIds(StuckTaskAlertFindCriteria.builder().escalated(true).build()))
                .contains(ALERT_ID_1, ALERT_ID_3).doesNotContain(ALERT_ID_0, ALERT_ID_2, ALERT_ID_4);
    }

    @Test
    void testFindByCreatorEmail() {
        assertThat(this.findIds(StuckTaskAlertFindCriteria.builder().creatorEmail(EMAIL_1).build()))
                .contains(ALERT_ID_2, ALERT_ID_3).doesNotContain(ALERT_ID_0, ALERT_ID_1, ALERT_ID_4);
    }

    @Test
    void testFindCombinedCriteria() {
        List<StuckTaskAlert> alerts = this.client.find(StuckTaskAlertFindCriteria.builder()
                .procedureKeyword("tax").withPenalty(true).escalated(true).creatorEmail(EMAIL_0).build());
        assertThat(alerts).extracting(StuckTaskAlert::getId)
                .contains(ALERT_ID_1).doesNotContain(ALERT_ID_0, ALERT_ID_2, ALERT_ID_3, ALERT_ID_4);
        assertThat(alerts).filteredOn(alert -> alert.getId().equals(ALERT_ID_1))
                .singleElement().satisfies(alert -> {
                    assertThat(alert.getReference()).isEqualTo("STA-2025-0002");
                    assertThat(alert.getStuckTaskRule().getName()).isEqualTo("Tax procedure inactivity");
                    assertThat(alert.getStuckTaskRule().getCreatedByUser().getEmail()).isEqualTo(EMAIL_0);
                    assertThat(alert.getStuckTaskRule().getCreatedByUser().getFirstName()).isEqualTo("cliente0");
                });
    }

    @Test
    void testFindUnknownEmail() {
        assertThat(this.client.find(StuckTaskAlertFindCriteria.builder()
                .creatorEmail("unknown-" + UUID.randomUUID() + "@example.com").build())).isEmpty();
    }

    @Test
    void testDeleteNotFound() {
        assertThatThrownBy(() -> this.client.delete(UNKNOWN_ID)).isInstanceOf(FeignException.NotFound.class);
    }
}
