package es.upm.miw.apaw.functionaltests.survey;

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

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = SurveyQuestionResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class SurveyQuestionResourceFT {
    private static final UUID QUESTION_ID = UUID.fromString("bbbbbbbb-cccc-dddd-eeee-ffffffff0000");
    private static final UUID UNKNOWN_ID = UUID.fromString("bbbbbbbb-cccc-dddd-eeee-ffffffff9999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = SurveyQuestionClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private SurveyQuestionClient client;

    private SurveyQuestion question() {
        return SurveyQuestion.builder().text("Feign question " + UUID.randomUUID())
                .surveyQuestionType(SurveyQuestionType.TEXT).options(List.of("Java", "Kotlin")).build();
    }

    @Test
    void testCreateUpdateAndDelete() {
        SurveyQuestion question = this.client.create(this.question());
        assertThat(question.getId()).isNotNull();
        assertThat(question.getSurveyQuestionType()).isEqualTo(SurveyQuestionType.TEXT);
        assertThat(question.getOptions()).containsExactly("Java", "Kotlin");

        question.setText("Feign question updated " + UUID.randomUUID());
        question.setSurveyQuestionType(SurveyQuestionType.YES_NO);
        question.setRequired(false);
        question.setMaxLength(5);
        question.setOptions(List.of("Yes", "No"));
        SurveyQuestion updated = this.client.update(question.getId(), question);
        assertThat(updated).usingRecursiveComparison().isEqualTo(question);
        assertThat(this.client.read(question.getId())).usingRecursiveComparison().isEqualTo(updated);

        this.client.delete(question.getId());
        assertThatThrownBy(() -> this.client.read(question.getId())).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateAppliesDefaults() {
        SurveyQuestion question = this.client.create(this.question());
        assertThat(question.getRequired()).isTrue();
        this.client.delete(question.getId());
    }

    @Test
    void testRead() {
        SurveyQuestion question = this.client.read(QUESTION_ID);
        assertThat(question.getId()).isEqualTo(QUESTION_ID);
        assertThat(question.getText()).isEqualTo("How satisfied are you?");
        assertThat(question.getSurveyQuestionType()).isEqualTo(SurveyQuestionType.RATING);
        assertThat(question.getRequired()).isTrue();
        assertThat(question.getMaxLength()).isEqualTo(2);
        assertThat(question.getOptions()).containsExactly("1", "2", "3", "4", "5");
    }

    @Test
    void testFindAll() {
        assertThat(this.client.findAll()).extracting(SurveyQuestion::getText)
                .contains("How satisfied are you?", "What is your main goal?", "Would you attend again?");
    }

    @Test
    void testReadNotFound() {
        assertThatThrownBy(() -> this.client.read(UNKNOWN_ID)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testUpdateNotFound() {
        assertThatThrownBy(() -> this.client.update(UNKNOWN_ID, this.question()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testDeleteReferencedQuestion() {
        assertThatThrownBy(() -> this.client.delete(QUESTION_ID)).isInstanceOf(FeignException.Conflict.class);
        assertThat(this.client.read(QUESTION_ID).getText()).isEqualTo("How satisfied are you?");
    }

    @Test
    void testDeleteUnknownQuestion() {
        this.client.delete(UNKNOWN_ID);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidText(String text) {
        SurveyQuestion question = this.question();
        question.setText(text);
        assertThatThrownBy(() -> this.client.create(question)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutType() {
        SurveyQuestion question = this.question();
        question.setSurveyQuestionType(null);
        assertThatThrownBy(() -> this.client.create(question)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutOptions() {
        SurveyQuestion question = this.question();
        question.setOptions(List.of());
        assertThatThrownBy(() -> this.client.create(question)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testUpdateInvalidText() {
        SurveyQuestion question = this.question();
        question.setText(" ");
        assertThatThrownBy(() -> this.client.update(QUESTION_ID, question))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testPatchText() {
        SurveyQuestion first = this.client.create(this.question());
        SurveyQuestion second = this.client.create(this.question());

        this.client.patchText(List.of(
                new SurveyQuestionTextPatch(first.getId(), "First patched text"),
                new SurveyQuestionTextPatch(second.getId(), "Second patched text")));

        assertThat(this.client.read(first.getId()).getText()).isEqualTo("First patched text");
        assertThat(this.client.read(second.getId()).getText()).isEqualTo("Second patched text");
        this.client.delete(first.getId());
        this.client.delete(second.getId());
    }

    @Test
    void testPatchTextRepeatedId() {
        SurveyQuestion question = this.client.create(this.question());
        List<SurveyQuestionTextPatch> patches = List.of(
                new SurveyQuestionTextPatch(question.getId(), "First patched text"),
                new SurveyQuestionTextPatch(question.getId(), "Second patched text"));

        assertThatThrownBy(() -> this.client.patchText(patches)).isInstanceOf(FeignException.BadRequest.class);
        assertThat(this.client.read(question.getId()).getText()).isEqualTo(question.getText());
        this.client.delete(question.getId());
    }

    @Test
    void testPatchTextNotFoundChangesNothing() {
        SurveyQuestion question = this.client.create(this.question());
        List<SurveyQuestionTextPatch> patches = List.of(
                new SurveyQuestionTextPatch(question.getId(), "Patched text"),
                new SurveyQuestionTextPatch(UNKNOWN_ID, "Unknown text"));

        assertThatThrownBy(() -> this.client.patchText(patches)).isInstanceOf(FeignException.NotFound.class);
        assertThat(this.client.read(question.getId()).getText()).isEqualTo(question.getText());
        this.client.delete(question.getId());
    }

    @Test
    void testPatchTextEmpty() {
        assertThatThrownBy(() -> this.client.patchText(List.of())).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testPatchTextWithoutId() {
        assertThatThrownBy(() -> this.client.patchText(List.of(new SurveyQuestionTextPatch(null, "Text"))))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testPatchTextWithoutText() {
        assertThatThrownBy(() -> this.client.patchText(List.of(new SurveyQuestionTextPatch(QUESTION_ID, " "))))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testPatchTextNullElement() {
        assertThatThrownBy(() -> this.client.patchText(Arrays.asList((SurveyQuestionTextPatch) null)))
                .isInstanceOf(FeignException.BadRequest.class);
    }
}