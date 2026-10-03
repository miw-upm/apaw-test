package es.upm.miw.apaw.functionaltests.notifications;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = NotificationResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class NotificationResourceFT {
    private static final String MOST_FAILED_EVENT_TYPE = "DEMO_FAILURE_REPORT_MOST_FAILED";
    private static final String SECOND_EVENT_TYPE = "DEMO_FAILURE_REPORT_SECOND";
    private static final String NO_FAILURE_EVENT_TYPE = "DEMO_FAILURE_REPORT_NONE";
    private static final UUID TEMPLATE_ID = UUID.fromString("ffffffff-aaaa-bbbb-cccc-ddddeeee0000");
    private static final UUID USER_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");
    private static final UUID UNKNOWN_ID = UUID.fromString("ffffffff-aaaa-bbbb-cccc-ddddeeee9999");

    private static final List<UUID> MOST_FAILED_NOTIFICATION_IDS = List.of(
            UUID.fromString("eeeeeeee-aaaa-bbbb-cccc-ddddeeee1001"),
            UUID.fromString("eeeeeeee-aaaa-bbbb-cccc-ddddeeee1002"),
            UUID.fromString("eeeeeeee-aaaa-bbbb-cccc-ddddeeee1003"));
    private static final List<UUID> SECOND_NOTIFICATION_IDS = List.of(
            UUID.fromString("eeeeeeee-aaaa-bbbb-cccc-ddddeeee2001"),
            UUID.fromString("eeeeeeee-aaaa-bbbb-cccc-ddddeeee2002"));

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EnableFeignClients(clients = NotificationClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private NotificationClient client;

    @Test
    void testFindSeededNotificationsByEventType() {
        List<Notification> notifications = this.client.find(
                NotificationFindCriteria.builder().eventType(MOST_FAILED_EVENT_TYPE).build());
        assertThat(notifications).extracting(Notification::getId)
                .containsExactlyInAnyOrderElementsOf(MOST_FAILED_NOTIFICATION_IDS);
        assertThat(notifications).allSatisfy(notification -> {
            assertThat(notification.getTitle()).isEqualTo("Delivery report demonstration");
            assertThat(notification.getNotificationTemplate().getEventType()).isEqualTo(MOST_FAILED_EVENT_TYPE);
            assertThat(notification.getRecipient()).isNotNull();
        });
    }

    @Test
    void testFindByPriorityAndSent() {
        assertThat(this.client.find(NotificationFindCriteria.builder()
                .eventType(MOST_FAILED_EVENT_TYPE).priority(Priority.HIGH).build()))
                .extracting(Notification::getId)
                .containsExactly(MOST_FAILED_NOTIFICATION_IDS.getFirst());

        assertThat(this.client.find(NotificationFindCriteria.builder()
                .eventType(SECOND_EVENT_TYPE).sent(true).build()))
                .extracting(Notification::getId)
                .containsExactly(SECOND_NOTIFICATION_IDS.get(1));

        assertThat(this.client.find(NotificationFindCriteria.builder()
                .eventType(SECOND_EVENT_TYPE).sent(false).build()))
                .extracting(Notification::getId)
                .containsExactly(SECOND_NOTIFICATION_IDS.get(0));
    }

    @Test
    void testFindUnknownEventType() {
        assertThat(this.client.find(NotificationFindCriteria.builder()
                .eventType("FT_UNKNOWN_EVENT_" + UUID.randomUUID()).build())).isEmpty();
    }

    @Test
    void testFindTemplateFailureReport() {
        assertThat(this.client.findTemplateFailureReport())
                .filteredOn(report -> report.getEventType().equals(MOST_FAILED_EVENT_TYPE))
                .singleElement().satisfies(report -> {
                    assertThat(report.getChannel()).isEqualTo(Channel.EMAIL);
                    assertThat(report.getTotalNotificationCount()).isEqualTo(3);
                    assertThat(report.getFailedNotificationCount()).isEqualTo(2);
                    assertThat(report.getFailureRate()).isCloseTo(200.0 / 3.0,
                            org.assertj.core.data.Offset.offset(0.01));
                });
        assertThat(this.client.findTemplateFailureReport())
                .filteredOn(report -> report.getEventType().equals(SECOND_EVENT_TYPE))
                .singleElement().satisfies(report -> {
                    assertThat(report.getChannel()).isEqualTo(Channel.SMS);
                    assertThat(report.getTotalNotificationCount()).isEqualTo(2);
                    assertThat(report.getFailedNotificationCount()).isEqualTo(1);
                    assertThat(report.getFailureRate()).isEqualTo(50.0);
                });
        assertThat(this.client.findTemplateFailureReport())
                .filteredOn(report -> report.getEventType().equals(NO_FAILURE_EVENT_TYPE))
                .singleElement().satisfies(report -> {
                    assertThat(report.getChannel()).isEqualTo(Channel.PUSH);
                    assertThat(report.getTotalNotificationCount()).isEqualTo(1);
                    assertThat(report.getFailedNotificationCount()).isZero();
                    assertThat(report.getFailureRate()).isZero();
                });
    }

    @Test
    void testCreateUsesDefaults() {
        CreationNotification creation = this.creation();
        creation.setPriority(null);

        Notification created = this.client.create(creation);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getTitle()).isEqualTo(creation.getTitle());
        assertThat(created.getMessage()).isEqualTo(creation.getMessage());
        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(created.getSentAt()).isNull();
        assertThat(created.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(created.getNotificationStatus()).isEqualTo(NotificationStatus.PENDING);
        assertThat(created.getNotificationTemplate().getId()).isEqualTo(TEMPLATE_ID);
        assertThat(created.getRecipient().getId()).isEqualTo(USER_ID);
        assertThat(this.client.find(NotificationFindCriteria.builder()
                .eventType("USER_REGISTERED")
                .recipientEmail("cliente0@example.com")
                .build()))
                .extracting(Notification::getId).contains(created.getId());
    }

    @Test
    void testCreateWithUnknownTemplate() {
        CreationNotification creation = this.creation();
        creation.setNotificationTemplateId(UNKNOWN_ID);
        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateWithUnknownUser() {
        CreationNotification creation = this.creation();
        creation.setUserId(UNKNOWN_ID);
        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateRejectsInvalidTitleAndMessage() {
        CreationNotification invalidTitle = this.creation();
        invalidTitle.setTitle(" ");
        assertThatThrownBy(() -> this.client.create(invalidTitle))
                .isInstanceOf(FeignException.BadRequest.class);

        CreationNotification invalidMessage = this.creation();
        invalidMessage.setMessage("m".repeat(256));
        assertThatThrownBy(() -> this.client.create(invalidMessage))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    private CreationNotification creation() {
        return CreationNotification.builder()
                .title("Functional test " + UUID.randomUUID())
                .message("Functional notification test")
                .notificationTemplateId(TEMPLATE_ID)
                .priority(Priority.HIGH)
                .userId(USER_ID)
                .build();
    }
}
