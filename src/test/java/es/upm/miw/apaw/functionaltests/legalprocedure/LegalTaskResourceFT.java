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

import java.util.UUID;

import static java.time.temporal.ChronoUnit.MICROS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

@SpringBootTest(classes = LegalTaskResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class LegalTaskResourceFT {
    private static final UUID TASK_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");
    private static final UUID UNKNOWN_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff9999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = LegalTaskClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private LegalTaskClient client;

    @Test
    void testCreateUpdateAndDelete() {
        LegalTask task = this.client.create(LegalTask.builder().title("Feign task").build());
        assertThat(task.getId()).isNotNull();
        assertThat(task.getTitle()).isEqualTo("Feign task");
        assertThat(task.getCreatingDate()).isNotNull();
        assertThat(task.getTaskStatus()).isEqualTo(TaskStatus.CURRENT);

        task.setTitle("Feign task updated");
        task.setNotes("Updated notes");
        task.setTaskStatus(TaskStatus.DEPRECATED);
        LegalTask updated = this.client.update(task.getId(), task);
        assertThat(updated).usingRecursiveComparison().ignoringFields("creatingDate").isEqualTo(task);
        assertThat(updated.getCreatingDate()).isCloseTo(task.getCreatingDate(), within(1, MICROS));
        assertThat(this.client.read(task.getId())).isEqualTo(updated);

        this.client.delete(task.getId());
        assertThatThrownBy(() -> this.client.read(task.getId())).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testRead() {
        LegalTask task = this.client.read(TASK_ID);
        assertThat(task.getId()).isEqualTo(TASK_ID);
        assertThat(task.getTitle()).isEqualTo("Review documentation");
        assertThat(task.getTaskStatus()).isEqualTo(TaskStatus.CURRENT);
        assertThat(task.getNotes()).isEqualTo("Review the documents provided by the client");
    }

    @Test
    void testFindAll() {
        assertThat(this.client.findAll()).extracting(LegalTask::getTitle)
                .contains("Review documentation", "Draft claim", "Prepare hearing");
    }

    @Test
    void testFindUsageReport() {
        // TASK_5 aparece solo en PROCEDURE_3, que está cerrado.
        assertThat(this.client.findUsageReport())
                .filteredOn(report -> report.getTaskTitle().equals("Review settlement agreement"))
                .singleElement().satisfies(report -> {
                    assertThat(report.getTotalUsageCount()).isEqualTo(1);
                    assertThat(report.getActiveUsageCount()).isZero();
                });
    }

    @Test
    void testCreateDuplicateTitle() {
        LegalTask task = LegalTask.builder().title("Review documentation").build();
        assertThatThrownBy(() -> this.client.create(task)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testUpdateDuplicateTitle() {
        LegalTask task = LegalTask.builder().title("Draft claim").taskStatus(TaskStatus.CURRENT).build();
        assertThatThrownBy(() -> this.client.update(TASK_ID, task)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testReadNotFound() {
        assertThatThrownBy(() -> this.client.read(UNKNOWN_ID)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testUpdateNotFound() {
        LegalTask task = LegalTask.builder().title("Unknown task").build();
        assertThatThrownBy(() -> this.client.update(UNKNOWN_ID, task)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testDeleteReferencedTask() {
        assertThatThrownBy(() -> this.client.delete(TASK_ID)).isInstanceOf(FeignException.Conflict.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidTitle(String title) {
        LegalTask task = LegalTask.builder().title(title).build();
        assertThatThrownBy(() -> this.client.create(task)).isInstanceOf(FeignException.BadRequest.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testUpdateInvalidTitle(String title) {
        LegalTask task = LegalTask.builder().title(title).build();
        assertThatThrownBy(() -> this.client.update(TASK_ID, task)).isInstanceOf(FeignException.BadRequest.class);
    }

}
