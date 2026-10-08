package es.upm.miw.apaw.functionaltests.taskmanagement;

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
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
        classes = TaskResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
class TaskResourceFT {

    private static final UUID TASK_ID_0 = UUID.fromString("dddddddd-eeee-ffff-aaaa-333344440000");

    private static final UUID TASK_ID_1 = UUID.fromString("dddddddd-eeee-ffff-aaaa-333344440001");

    private static final UUID TASK_ID_2 = UUID.fromString("dddddddd-eeee-ffff-aaaa-333344440002");

    private static final UUID USER_ID_0 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");

    private static final UUID USER_ID_9 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0009");

    private static final UUID UNKNOWN_COMMENT_ID = UUID.fromString("cccccccc-dddd-eeee-ffff-111122229999");

    private static final UUID UNKNOWN_USER_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff9999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = {TaskClient.class, TaskCommentClient.class})
    static class ClientConfiguration {
    }

    @Autowired
    private TaskClient taskClient;

    @Autowired
    private TaskCommentClient taskCommentClient;

    @Test
    void testCreate() {
        TaskComment comment = this.createComment();

        CreationTask creation = CreationTask.builder()
                .title("Feign task " + UUID.randomUUID())
                .description("Functional test task")
                .dueDate(LocalDate.now().plusDays(30))
                .estimatedHours(new BigDecimal("5.50"))
                .taskCommentIds(List.of(comment.getId()))
                .ownerId(USER_ID_9)
                .build();

        Task actual = this.taskClient.create(creation);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getTitle()).isEqualTo(creation.getTitle());
        assertThat(actual.getDescription()).isEqualTo(creation.getDescription());
        assertThat(actual.getDueDate()).isEqualTo(creation.getDueDate());
        assertThat(actual.getPriority()).isEqualTo(3);
        assertThat(actual.getCompletion()).isFalse();

        assertThat(actual.getEstimatedHours()).isEqualByComparingTo("5.50");

        assertThat(actual.getComments()).extracting(TaskComment::getId)
                .containsExactly(comment.getId());

        assertThat(actual.getOwner().getId()).isEqualTo(USER_ID_9);
        assertThat(actual.getOwner().getFirstName()).isEqualTo("Cliente9");
    }

    @Test
    void testCreateEmptyTaskComments() {
        CreationTask creation = this.creation();
        creation.setTitle("Empty comments " + UUID.randomUUID());
        creation.setTaskCommentIds(List.of());

        Task actual = this.taskClient.create(creation);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getComments()).isEmpty();
    }

    @Test
    void testCreateDuplicateTitle() {
        CreationTask creation = this.creation();
        creation.setTitle("Prepare project proposal");

        assertThatThrownBy(() -> this.taskClient.create(creation))
                .isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateUnknownTaskComment() {
        CreationTask creation = this.creation();
        creation.setTaskCommentIds(List.of(UNKNOWN_COMMENT_ID));

        assertThatThrownBy(() -> this.taskClient.create(creation))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateUnknownOwner() {
        CreationTask creation = this.creation();
        creation.setOwnerId(UNKNOWN_USER_ID);

        assertThatThrownBy(() -> this.taskClient.create(creation))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidTitle(String title) {
        CreationTask creation = this.creation();
        creation.setTitle(title);

        assertThatThrownBy(() -> this.taskClient.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutDueDate() {
        CreationTask creation = this.creation();
        creation.setDueDate(null);

        assertThatThrownBy(() -> this.taskClient.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutOwner() {
        CreationTask creation = this.creation();
        creation.setOwnerId(null);

        assertThatThrownBy(() -> this.taskClient.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutTaskComments() {
        CreationTask creation = this.creation();
        creation.setTaskCommentIds(null);

        assertThatThrownBy(() -> this.taskClient.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateNullTaskCommentId() {
        CreationTask creation = this.creation();
        creation.setTaskCommentIds(Arrays.asList((UUID) null));

        assertThatThrownBy(() -> this.taskClient.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testFindAll() {
        assertThat(this.taskClient.find(new TaskFindCriteria()))
                .extracting(Task::getId)
                .contains(TASK_ID_0, TASK_ID_1, TASK_ID_2);
    }

    @Test
    void testFindByPriority() {
        assertThat(this.taskClient.find(
                TaskFindCriteria.builder()
                        .priority(2)
                        .build()))
                .extracting(Task::getId)
                .contains(TASK_ID_1)
                .doesNotContain(TASK_ID_0, TASK_ID_2);
    }

    @Test
    void testFindByOverdue() {
        assertThat(this.taskClient.find(
                TaskFindCriteria.builder()
                        .overdue(true)
                        .build()))
                .extracting(Task::getId)
                .contains(TASK_ID_0, TASK_ID_1)
                .doesNotContain(TASK_ID_2);
    }

    @Test
    void testFindByType() {
        assertThat(this.taskClient.find(
                TaskFindCriteria.builder()
                        .type(CommentType.IMPORTANT)
                        .build()))
                .extracting(Task::getId)
                .contains(TASK_ID_0)
                .doesNotContain(TASK_ID_1, TASK_ID_2);
    }

    @Test
    void testFindByOwnerFirstName() {
        List<Task> tasks = this.taskClient.find(
                TaskFindCriteria.builder()
                        .ownerFirstName("cliente0")
                        .build()
        );

        assertThat(tasks).extracting(Task::getId).contains(TASK_ID_0).doesNotContain(TASK_ID_1, TASK_ID_2);

        assertThat(tasks).filteredOn(task -> task.getId().equals(TASK_ID_0))
                .singleElement()
                .satisfies(task -> {
                    assertThat(task.getOwner().getId()).isEqualTo(USER_ID_0);

                    assertThat(task.getOwner().getFirstName()).isEqualTo("cliente0");
                });
    }

    @Test
    void testFindCombinedCriteria() {
        assertThat(this.taskClient.find(
                TaskFindCriteria.builder()
                        .priority(1)
                        .overdue(true)
                        .type(CommentType.IMPORTANT)
                        .ownerFirstName("cliente0")
                        .build()))
                .extracting(Task::getId)
                .contains(TASK_ID_0)
                .doesNotContain(TASK_ID_1, TASK_ID_2);
    }

    @Test
    void testFindUnknownOwnerFirstName() {
        assertThat(this.taskClient.find(
                TaskFindCriteria.builder()
                        .ownerFirstName("unknown-" + UUID.randomUUID())
                        .build()))
                .isEmpty();
    }

    @Test
    void testFindBlankOwnerFirstNameDoesNotFilter() {
        assertThat(this.taskClient.find(
                TaskFindCriteria.builder()
                        .ownerFirstName(" ")
                        .build()))
                .extracting(Task::getId)
                .contains(TASK_ID_0, TASK_ID_1, TASK_ID_2);
    }

    @Test
    void testFindActivityReport() {
        List<TaskActivityReport> report = this.taskClient.findActivityReport();

        assertThat(report).extracting(TaskActivityReport::getTotalCommentCount)
                .isSortedAccordingTo(Comparator.reverseOrder());

        assertThat(report)
                .filteredOn(item -> item.getTaskTitle().equals("Prepare project proposal"))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.getTotalCommentCount()).isGreaterThanOrEqualTo(2);

                    assertThat(item.getImportantCommentCount()).isGreaterThanOrEqualTo(1);

                    assertThat(item.getEditedCommentCount()).isGreaterThanOrEqualTo(1);

                    assertThat(item.getAttachmentCommentCount()).isLessThanOrEqualTo(item.getTotalCommentCount());

                    assertThat(item.getOwner().getId()).isEqualTo(USER_ID_0);

                    assertThat(item.getOwner().getFirstName()).isEqualTo("cliente0");
                });
    }

    private CreationTask creation() {
        return CreationTask.builder()
                .title("Feign task " + UUID.randomUUID())
                .dueDate(LocalDate.now().plusDays(30))
                .taskCommentIds(List.of())
                .ownerId(USER_ID_9)
                .build();
    }

    private TaskComment createComment() {
        return this.taskCommentClient.create(
                TaskComment.builder()
                        .content("Feign Task comment " + UUID.randomUUID())
                        .author(
                                UserSnapshot.builder()
                                        .id(USER_ID_9)
                                        .build()
                        )
                        .build()
        );
    }
}
