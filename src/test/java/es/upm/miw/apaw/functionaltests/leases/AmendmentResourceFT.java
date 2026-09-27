package es.upm.miw.apaw.functionaltests.leases;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = AmendmentResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class AmendmentResourceFT {
    private static final UUID AMENDMENT_ID = UUID.fromString("dddddddd-eeee-ffff-aaaa-bbbbcccc0000");
    private static final UUID SCOPE_AMENDMENT_ID = UUID.fromString("dddddddd-eeee-ffff-aaaa-bbbbcccc0002");
    private static final UUID TERM_AMENDMENT_ID = UUID.fromString("dddddddd-eeee-ffff-aaaa-bbbbcccc0001");
    private static final UUID UNKNOWN_ID = UUID.fromString("dddddddd-eeee-ffff-aaaa-bbbbcccc9999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = AmendmentClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private AmendmentClient client;

    private Amendment newAmendment() {
        return Amendment.builder().amendmentNumber(1).description("Feign amendment")
                .effectiveDate(LocalDate.of(2026, 1, 1)).additionalAmount(new BigDecimal("30.00"))
                .amendmentType(AmendmentType.PRICE_CHANGE).build();
    }

    @Test
    void testCreateUpdatePatchAndDelete() {
        Amendment amendment = this.client.create(this.newAmendment());
        assertThat(amendment.getId()).isNotNull();
        assertThat(amendment.getDescription()).isEqualTo("Feign amendment");
        assertThat(amendment.getApproved()).isFalse();

        amendment.setDescription("Feign amendment updated");
        amendment.setAdditionalAmount(null);
        amendment.setAmendmentType(AmendmentType.TERMINATION);
        Amendment updated = this.client.update(amendment.getId(), amendment);
        assertThat(updated).isEqualTo(amendment);

        Amendment patched = this.client.patch(amendment.getId(), AmendmentUpdate.builder().approved(true).build());
        assertThat(patched.getApproved()).isTrue();
        assertThat(patched.getDescription()).isEqualTo("Feign amendment updated");
        assertThat(patched.getAmendmentType()).isEqualTo(AmendmentType.TERMINATION);
        assertThat(this.client.read(amendment.getId())).isEqualTo(patched);

        this.client.delete(amendment.getId());
        assertThatThrownBy(() -> this.client.read(amendment.getId())).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testRead() {
        Amendment amendment = this.client.read(AMENDMENT_ID);
        assertThat(amendment.getId()).isEqualTo(AMENDMENT_ID);
        assertThat(amendment.getDescription()).isEqualTo("Annual rent update according to CPI");
        assertThat(amendment.getAdditionalAmount()).isEqualByComparingTo("25.00");
        assertThat(amendment.getApproved()).isTrue();
        assertThat(amendment.getAmendmentType()).isEqualTo(AmendmentType.PRICE_CHANGE);
    }

    @Test
    void testFindAllSortedByEffectiveDate() {
        assertThat(this.client.findAll()).extracting(Amendment::getId)
                .containsSubsequence(AMENDMENT_ID, SCOPE_AMENDMENT_ID, TERM_AMENDMENT_ID);
    }

    @Test
    void testReadNotFound() {
        assertThatThrownBy(() -> this.client.read(UNKNOWN_ID)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testUpdateNotFound() {
        assertThatThrownBy(() -> this.client.update(UNKNOWN_ID, this.newAmendment()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testPatchNotFound() {
        AmendmentUpdate update = AmendmentUpdate.builder().approved(true).build();
        assertThatThrownBy(() -> this.client.patch(UNKNOWN_ID, update)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testDeleteReferencedAmendment() {
        assertThatThrownBy(() -> this.client.delete(AMENDMENT_ID)).isInstanceOf(FeignException.Conflict.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidDescription(String description) {
        Amendment amendment = this.newAmendment();
        amendment.setDescription(description);
        assertThatThrownBy(() -> this.client.create(amendment)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutEffectiveDate() {
        Amendment amendment = this.newAmendment();
        amendment.setEffectiveDate(null);
        assertThatThrownBy(() -> this.client.create(amendment)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testUpdateWithoutType() {
        Amendment amendment = this.newAmendment();
        amendment.setAmendmentType(null);
        assertThatThrownBy(() -> this.client.update(AMENDMENT_ID, amendment))
                .isInstanceOf(FeignException.BadRequest.class);
    }
}
