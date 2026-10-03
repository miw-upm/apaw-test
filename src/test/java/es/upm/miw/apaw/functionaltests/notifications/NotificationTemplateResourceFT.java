package es.upm.miw.apaw.functionaltests.notifications;

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

@SpringBootTest(classes = NotificationTemplateResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class NotificationTemplateResourceFT {
    private static final UUID TEMPLATE_0_ID = UUID.fromString("ffffffff-aaaa-bbbb-cccc-ddddeeee0000");
    private static final UUID TEMPLATE_1_ID = UUID.fromString("ffffffff-aaaa-bbbb-cccc-ddddeeee0001");
    private static final UUID TEMPLATE_2_ID = UUID.fromString("ffffffff-aaaa-bbbb-cccc-ddddeeee0002");
    private static final UUID TEMPLATE_3_ID = UUID.fromString("ffffffff-aaaa-bbbb-cccc-ddddeeee0003");
    private static final UUID UNKNOWN_ID = UUID.fromString("ffffffff-aaaa-bbbb-cccc-ddddeeee9999");

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EnableFeignClients(clients = NotificationTemplateClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private NotificationTemplateClient client;

    @Test
    void testReadSeeder() {
        NotificationTemplate template = this.client.read(TEMPLATE_0_ID);
        assertThat(template).usingRecursiveComparison().isEqualTo(NotificationTemplate.builder()
                .id(TEMPLATE_0_ID)
                .eventType("USER_REGISTERED")
                .subjectTemplate("Welcome, {{firstName}}")
                .bodyTemplate("Hello {{firstName}}, your account has been created.")
                .channel(Channel.EMAIL)
                .build());
    }

    @Test
    void testReadSeederAtMaximumFieldLengths() {
        NotificationTemplate template = this.client.read(TEMPLATE_3_ID);
        assertThat(template.getEventType()).hasSize(60).isEqualTo("e".repeat(60));
        assertThat(template.getSubjectTemplate()).hasSize(60).isEqualTo("s".repeat(60));
        assertThat(template.getBodyTemplate()).hasSize(500).isEqualTo("b".repeat(500));
    }

    @Test
    void testFindAllContainsSeededTemplates() {
        assertThat(this.client.findAll()).extracting(NotificationTemplate::getId)
                .contains(TEMPLATE_0_ID, TEMPLATE_1_ID, TEMPLATE_2_ID, TEMPLATE_3_ID);
    }

    @Test
    void testReadNotFound() {
        assertThatThrownBy(() -> this.client.read(UNKNOWN_ID))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateAndDelete() {
        NotificationTemplate request = this.template("FT_" + UUID.randomUUID());
        request.setChannel(null);

        NotificationTemplate created = this.client.create(request);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getEventType()).isEqualTo(request.getEventType());
        assertThat(created.getChannel()).isEqualTo(Channel.EMAIL);

        this.client.delete(created.getId());
        assertThatThrownBy(() -> this.client.read(created.getId()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateDuplicateEventType() {
        NotificationTemplate duplicate = this.template("USER_REGISTERED");
        assertThatThrownBy(() -> this.client.create(duplicate))
                .isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateRejectsOversizedFields() {
        NotificationTemplate eventTypeTooLong = this.template("e".repeat(61));
        assertThatThrownBy(() -> this.client.create(eventTypeTooLong))
                .isInstanceOf(FeignException.BadRequest.class);

        NotificationTemplate subjectTooLong = this.template("FT_" + UUID.randomUUID());
        subjectTooLong.setSubjectTemplate("s".repeat(61));
        assertThatThrownBy(() -> this.client.create(subjectTooLong))
                .isInstanceOf(FeignException.BadRequest.class);

        NotificationTemplate bodyTooLong = this.template("FT_" + UUID.randomUUID());
        bodyTooLong.setBodyTemplate("b".repeat(501));
        assertThatThrownBy(() -> this.client.create(bodyTooLong))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateRejectsBlankFields() {
        NotificationTemplate eventTypeBlank = this.template(" ");
        assertThatThrownBy(() -> this.client.create(eventTypeBlank))
                .isInstanceOf(FeignException.BadRequest.class);

        NotificationTemplate subjectBlank = this.template("FT_" + UUID.randomUUID());
        subjectBlank.setSubjectTemplate(" ");
        assertThatThrownBy(() -> this.client.create(subjectBlank))
                .isInstanceOf(FeignException.BadRequest.class);

        NotificationTemplate bodyBlank = this.template("FT_" + UUID.randomUUID());
        bodyBlank.setBodyTemplate(" ");
        assertThatThrownBy(() -> this.client.create(bodyBlank))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testUpdateReplacesTemplateFields() {
        NotificationTemplate original = this.client.create(this.template("FT_" + UUID.randomUUID()));
        NotificationTemplate replacement = NotificationTemplate.builder()
                .eventType("FT_UPDATED_" + UUID.randomUUID())
                .subjectTemplate("Updated subject")
                .bodyTemplate("Updated body")
                .channel(Channel.PUSH)
                .build();

        NotificationTemplate updated = this.client.update(original.getId(), replacement);
        assertThat(updated.getId()).isEqualTo(original.getId());
        assertThat(updated.getEventType()).isEqualTo(replacement.getEventType());
        assertThat(updated.getSubjectTemplate()).isEqualTo(replacement.getSubjectTemplate());
        assertThat(updated.getBodyTemplate()).isEqualTo(replacement.getBodyTemplate());
        assertThat(updated.getChannel()).isEqualTo(replacement.getChannel());
        this.client.delete(updated.getId());
    }

    @Test
    void testUpdateNotFound() {
        assertThatThrownBy(() -> this.client.update(UNKNOWN_ID, this.template("FT_" + UUID.randomUUID())))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testPatchUpdatesOnlyProvidedField() {
        NotificationTemplate original = this.client.create(this.template("FT_" + UUID.randomUUID()));
        String updatedSubject = "Patched subject";
        NotificationTemplate patch = NotificationTemplate.builder().subjectTemplate(updatedSubject).build();

        NotificationTemplate updated = this.client.patch(original.getId(), patch);
        assertThat(updated.getSubjectTemplate()).isEqualTo(updatedSubject);
        assertThat(updated.getEventType()).isEqualTo(original.getEventType());
        assertThat(updated.getBodyTemplate()).isEqualTo(original.getBodyTemplate());
        assertThat(updated.getChannel()).isEqualTo(original.getChannel());
        this.client.delete(updated.getId());
    }

    @Test
    void testPatchRejectsOversizedField() {
        NotificationTemplate patch = NotificationTemplate.builder().bodyTemplate("b".repeat(501)).build();
        assertThatThrownBy(() -> this.client.patch(TEMPLATE_0_ID, patch))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testDeleteReferencedTemplate() {
        UUID referencedTemplateId = UUID.fromString("eeeeeeee-aaaa-bbbb-cccc-ddddeeee0001");
        assertThatThrownBy(() -> this.client.delete(referencedTemplateId))
                .isInstanceOf(FeignException.Conflict.class);
    }

    private NotificationTemplate template(String eventType) {
        return NotificationTemplate.builder()
                .eventType(eventType)
                .subjectTemplate("Functional test subject")
                .bodyTemplate("Functional test body")
                .channel(Channel.EMAIL)
                .build();
    }
}
