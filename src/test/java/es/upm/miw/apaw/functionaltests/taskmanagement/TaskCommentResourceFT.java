package es.upm.miw.apaw.functionaltests.taskmanagement;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
        classes = TaskCommentResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
class TaskCommentResourceFT {

    private static final UUID COMMENT_ID_0 = UUID.fromString("cccccccc-dddd-eeee-ffff-111122220000");

    private static final UUID COMMENT_ID_1 = UUID.fromString("cccccccc-dddd-eeee-ffff-111122220001");

    private static final UUID COMMENT_ID_2 = UUID.fromString("cccccccc-dddd-eeee-ffff-111122220002");

    private static final UUID COMMENT_ID_3 = UUID.fromString("cccccccc-dddd-eeee-ffff-111122220003");

    private static final UUID USER_ID_0 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");

    private static final UUID UNKNOWN_ID = UUID.fromString("cccccccc-dddd-eeee-ffff-111122229999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = TaskCommentClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private TaskCommentClient client;

    @Test
    void testCreateUpdateAndDelete() {
        TaskComment comment = this.createComment();

        assertThat(comment.getId()).isNotNull();
        assertThat(comment.getContent()).startsWith("Feign comment");
        assertThat(comment.getCreationDate()).isNotNull();
        assertThat(comment.getEdition()).isFalse();
        assertThat(comment.getAttachment()).isFalse();
        assertThat(comment.getType()).isEqualTo(CommentType.GENERAL);

        TaskComment replacement = TaskComment.builder()
                .content("Feign comment updated")
                .edition(true)
                .attachment(true)
                .type(CommentType.IMPORTANT)
                .author(UserSnapshot.builder().id(USER_ID_0).build())
                .build();

        TaskComment updated = this.client.update(comment.getId(), replacement);

        assertThat(updated.getId()).isEqualTo(comment.getId());
        assertThat(updated.getContent()).isEqualTo("Feign comment updated");
        assertThat(updated.getEdition()).isTrue();
        assertThat(updated.getAttachment()).isTrue();
        assertThat(updated.getType()).isEqualTo(CommentType.IMPORTANT);

        assertThat(this.client.read(comment.getId()).getId()).isEqualTo(comment.getId());

        this.client.delete(comment.getId());

        assertThatThrownBy(() -> this.client.read(comment.getId())).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testRead() {
        TaskComment comment = this.client.read(COMMENT_ID_0);

        assertThat(comment.getId()).isEqualTo(COMMENT_ID_0);
        assertThat(comment.getContent()).isEqualTo("Initial requirements reviewed");
        assertThat(comment.getEdition()).isFalse();
        assertThat(comment.getAttachment()).isFalse();
        assertThat(comment.getType()).isEqualTo(CommentType.GENERAL);
    }

    @Test
    void testFindAll() {
        assertThat(this.client.findAll()).extracting(TaskComment::getId)
                .containsSubsequence(COMMENT_ID_3, COMMENT_ID_2, COMMENT_ID_1, COMMENT_ID_0);
    }

    @Test
    void testReadNotFound() {
        assertThatThrownBy(() -> this.client.read(UNKNOWN_ID)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testUpdateNotFound() {
        TaskComment replacement = TaskComment.builder()
                .content("Unknown comment")
                .edition(false)
                .attachment(false)
                .type(CommentType.GENERAL)
                .author(UserSnapshot.builder().id(USER_ID_0).build())
                .build();

        assertThatThrownBy(() -> this.client.update(UNKNOWN_ID, replacement))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testDeleteReferencedComment() {
        assertThatThrownBy(() -> this.client.delete(COMMENT_ID_0)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateBlankContent() {
        TaskComment comment = TaskComment.builder()
                .content(" ")
                .author(UserSnapshot.builder().id(USER_ID_0).build())
                .build();

        assertThatThrownBy(() -> this.client.create(comment)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateMissingAuthor() {
        TaskComment comment = TaskComment.builder()
                .content("Comment without author")
                .build();

        assertThatThrownBy(() -> this.client.create(comment)).isInstanceOf(FeignException.BadRequest.class);
    }

    private TaskComment createComment() {
        return this.client.create(
                TaskComment.builder()
                        .content("Feign comment " + UUID.randomUUID())
                        .author(
                                UserSnapshot.builder()
                                        .id(USER_ID_0)
                                        .build()
                        )
                        .build()
        );
    }
}
