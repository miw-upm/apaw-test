package es.upm.miw.apaw.functionaltests.legalprocedure;

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

@SpringBootTest(classes = LegalProcedureResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class LegalProcedureResourceFT {
    private static final UUID TASK_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");
    private static final UUID UNKNOWN_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff9999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = LegalProcedureClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private LegalProcedureClient client;

    private static final UUID PROCEDURE_ID = UUID.fromString("bbbbbbbb-cccc-dddd-eeee-ffffffff0000");
    private static final UUID CLOSED_ID = UUID.fromString("bbbbbbbb-cccc-dddd-eeee-ffffffff0001");

    private CreationLegalProcedure creation() {
        return CreationLegalProcedure.builder().title("Feign procedure")
                .budget(new BigDecimal("500.00")).legalTaskIds(List.of(TASK_ID))
                .userId(TASK_ID).build();
    }

    @Test
    void testCreate() {
        CreationLegalProcedure creation = this.creation();
        // No hay DELETE de procedimientos: se usa un título único en cada ejecución.
        creation.setTitle("Feign procedure " + UUID.randomUUID());
        creation.setLegalTaskIds(List.of(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0004")));
        creation.setUserId(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0009"));
        creation.setBudgetProposal("Fixed fee");

        LegalProcedure actual = this.client.create(creation);
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getTitle()).isEqualTo(creation.getTitle());
        assertThat(actual.getBudget()).isEqualByComparingTo(creation.getBudget());
        assertThat(actual.getBudgetProposal()).isEqualTo("Fixed fee");
        assertThat(actual.getStartedDate()).isEqualTo(LocalDate.now());
        assertThat(actual.getClosingDate()).isNull();
        assertThat(actual.getVatIncluded()).isFalse();
        assertThat(actual.getLegalTasks()).extracting(LegalTask::getId)
                .containsExactlyElementsOf(creation.getLegalTaskIds());
        assertThat(actual.getUserSnapshot().getId()).isEqualTo(creation.getUserId());
        assertThat(actual.getUserSnapshot().getMobile()).isEqualTo("600000109");
        assertThat(this.client.find(LegalProcedureFindCriteria.builder().userMobile("600000109").build()))
                .extracting(LegalProcedure::getId).contains(actual.getId());
    }

    @Test
    void testFindAll() {
        assertThat(this.client.find(new LegalProcedureFindCriteria())).extracting(LegalProcedure::getId)
                .contains(PROCEDURE_ID, CLOSED_ID);
    }

    @Test
    void testFindByUserMobileReturnsSummary() {
        assertThat(this.client.find(LegalProcedureFindCriteria.builder().userMobile("600000100").build()))
                .singleElement().satisfies(procedure -> {
                    assertThat(procedure.getId()).isEqualTo(PROCEDURE_ID);
                    assertThat(procedure.getTitle()).isEqualTo("Employment contract review");
                    assertThat(procedure.getBudget()).isEqualByComparingTo("850.00");
                    assertThat(procedure.getLegalTasks()).isNull();
                    assertThat(procedure.getBudgetProposal()).isNull();
                    assertThat(procedure.getUserSnapshot().getMobile()).isEqualTo("600000100");
                    assertThat(procedure.getUserSnapshot().getFirstName()).isEqualTo("cliente0");
                });
    }

    @Test
    void testFindByOpened() {
        assertThat(this.client.find(LegalProcedureFindCriteria.builder().opened(true).build()))
                .extracting(LegalProcedure::getId).contains(PROCEDURE_ID).doesNotContain(CLOSED_ID);
        assertThat(this.client.find(LegalProcedureFindCriteria.builder().opened(false).build()))
                .extracting(LegalProcedure::getId).contains(CLOSED_ID).doesNotContain(PROCEDURE_ID);
    }

    @Test
    void testFindByVatIncluded() {
        assertThat(this.client.find(LegalProcedureFindCriteria.builder().vatIncluded(false).build()))
                .extracting(LegalProcedure::getId).contains(PROCEDURE_ID);
        assertThat(this.client.find(LegalProcedureFindCriteria.builder().vatIncluded(true).build()))
                .extracting(LegalProcedure::getId).doesNotContain(PROCEDURE_ID);
    }

    @Test
    void testFindByTaskStatus() {
        assertThat(this.client.find(LegalProcedureFindCriteria.builder().taskStatus(TaskStatus.WITHDRAWN).build()))
                .extracting(LegalProcedure::getId).contains(CLOSED_ID).doesNotContain(PROCEDURE_ID);
    }

    @Test
    void testFindCombinedCriteria() {
        assertThat(this.client.find(LegalProcedureFindCriteria.builder().userMobile("600000100")
                .opened(true).vatIncluded(false).taskStatus(TaskStatus.CURRENT).build()))
                .extracting(LegalProcedure::getId).containsExactly(PROCEDURE_ID);
    }

    @Test
    void testFindUnknownMobile() {
        assertThat(this.client.find(LegalProcedureFindCriteria.builder().userMobile("699999998").build())).isEmpty();
    }

    @Test
    void testCreateDuplicateTitle() {
        CreationLegalProcedure creation = this.creation();
        creation.setTitle("Employment contract review");
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateUnknownTask() {
        CreationLegalProcedure creation = this.creation();
        creation.setLegalTaskIds(List.of(UNKNOWN_ID));
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateUnknownUser() {
        CreationLegalProcedure creation = this.creation();
        creation.setUserId(UNKNOWN_ID);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.NotFound.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidTitle(String title) {
        CreationLegalProcedure creation = this.creation();
        creation.setTitle(title);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutBudget() {
        CreationLegalProcedure creation = this.creation();
        creation.setBudget(null);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutUser() {
        CreationLegalProcedure creation = this.creation();
        creation.setUserId(null);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutTasks() {
        CreationLegalProcedure creation = this.creation();
        creation.setLegalTaskIds(null);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateEmptyTasks() {
        CreationLegalProcedure creation = this.creation();
        creation.setLegalTaskIds(List.of());
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateNullTaskId() {
        CreationLegalProcedure creation = this.creation();
        creation.setLegalTaskIds(Arrays.asList((UUID) null));
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }
}
