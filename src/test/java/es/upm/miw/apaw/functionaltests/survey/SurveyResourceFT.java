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

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = SurveyResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class SurveyResourceFT {
    private static final UUID QUESTION_0 = UUID.fromString("bbbbbbbb-cccc-dddd-eeee-ffffffff0000");
    private static final UUID QUESTION_2 = UUID.fromString("bbbbbbbb-cccc-dddd-eeee-ffffffff0002");
    private static final UUID UNKNOWN_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff9999");
    private static final UUID USER_0 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");
    private static final UUID USER_3 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0003");
    private static final UUID USER_9 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0009");
    private static final UUID SURVEY_0 = UUID.fromString("cccccccc-dddd-eeee-ffff-aaaaaaaa0000");
    private static final UUID SURVEY_1 = UUID.fromString("cccccccc-dddd-eeee-ffff-aaaaaaaa0001");
    private static final UUID SURVEY_2 = UUID.fromString("cccccccc-dddd-eeee-ffff-aaaaaaaa0002");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = SurveyClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private SurveyClient client;

    private CreationSurvey creation() {
        // No hay DELETE de surveys: se usa un título único en cada ejecución.
        return CreationSurvey.builder().title("Feign survey " + UUID.randomUUID())
                .description("Feign survey description").surveyQuestionIds(List.of(QUESTION_0, QUESTION_2))
                .userId(USER_9).build();
    }

    @Test
    void testCreate() {
        CreationSurvey creation = this.creation();

        Survey actual = this.client.create(creation);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getTitle()).isEqualTo(creation.getTitle());
        assertThat(actual.getDescription()).isEqualTo(creation.getDescription());
        assertThat(actual.getCreatedDate()).isIn(LocalDate.now(), LocalDate.now(ZoneOffset.UTC));
        assertThat(actual.getSubmittedDate()).isNull();
        assertThat(actual.getLanguage()).isEqualTo("Spanish");
        assertThat(actual.getSurveyQuestions()).extracting(SurveyQuestion::getId)
                .containsExactly(QUESTION_0, QUESTION_2);
        assertThat(actual.getUserSnapshot().getId()).isEqualTo(USER_9);
        assertThat(actual.getUserSnapshot().getMobile()).isEqualTo("600000109");
        assertThat(this.client.find(SurveyFindCriteria.builder().language("Spanish").build()))
                .extracting(Survey::getId).contains(actual.getId());
    }

    @Test
    void testCreateKeepsLanguage() {
        CreationSurvey creation = this.creation();
        creation.setLanguage("English");
        assertThat(this.client.create(creation).getLanguage()).isEqualTo("English");
    }

    @Test
    void testFindAll() {
        assertThat(this.client.find(new SurveyFindCriteria())).extracting(Survey::getId)
                .contains(SURVEY_0, SURVEY_1, SURVEY_2);
    }

    @Test
    void testFindByCityReturnsSummary() {
        assertThat(this.client.find(SurveyFindCriteria.builder().userCity("Sevilla").build()))
                .filteredOn(survey -> survey.getId().equals(SURVEY_1))
                .singleElement().satisfies(survey -> {
                    assertThat(survey.getTitle()).isEqualTo("Student goals");
                    assertThat(survey.getLanguage()).isEqualTo("Spanish");
                    assertThat(survey.getSurveyQuestions()).isNull();
                    assertThat(survey.getUserSnapshot().getMobile()).isEqualTo("600000101");
                    assertThat(survey.getUserSnapshot().getFirstName()).isEqualTo("cliente1");
                });
    }

    @Test
    void testFindByLanguage() {
        assertThat(this.client.find(SurveyFindCriteria.builder().language("English").build()))
                .extracting(Survey::getId).contains(SURVEY_0, SURVEY_2).doesNotContain(SURVEY_1);
    }

    @Test
    void testFindBySubmitted() {
        assertThat(this.client.find(SurveyFindCriteria.builder().submitted(true).build()))
                .extracting(Survey::getId).contains(SURVEY_0).doesNotContain(SURVEY_1, SURVEY_2);
        assertThat(this.client.find(SurveyFindCriteria.builder().submitted(false).build()))
                .extracting(Survey::getId).contains(SURVEY_1, SURVEY_2).doesNotContain(SURVEY_0);
    }

    @Test
    void testFindBySurveyQuestionType() {
        assertThat(this.client.find(SurveyFindCriteria.builder().surveyQuestionType(SurveyQuestionType.TEXT).build()))
                .extracting(Survey::getId).contains(SURVEY_1, SURVEY_2).doesNotContain(SURVEY_0)
                .doesNotHaveDuplicates();
    }

    @Test
    void testFindByUserCityIgnoringCase() {
        assertThat(this.client.find(SurveyFindCriteria.builder().userCity("madrid").build()))
                .extracting(Survey::getId).contains(SURVEY_0, SURVEY_2).doesNotContain(SURVEY_1);
    }

    @Test
    void testFindCombinedCriteria() {
        assertThat(this.client.find(SurveyFindCriteria.builder().language("English").submitted(false)
                .surveyQuestionType(SurveyQuestionType.YES_NO).userCity("Madrid").build()))
                .extracting(Survey::getId).containsExactly(SURVEY_2);
    }

    @Test
    void testFindUnknownCity() {
        assertThat(this.client.find(SurveyFindCriteria.builder().userCity("Atlantis").build())).isEmpty();
    }

    @Test
    void testFindUserLanguageReport() {
        List<SurveyUserLanguageReport> report = this.client.findUserLanguageReport();

        assertThat(report).extracting(SurveyUserLanguageReport::getSurveyQuestionCount)
                .isSortedAccordingTo(Comparator.reverseOrder());
        assertThat(report)
                .filteredOn(row -> row.getUserSnapshot().getId().equals(USER_0) && row.getLanguage().equals("English"))
                .singleElement().satisfies(row -> {
                    assertThat(row.getUserSnapshot().getMobile()).isEqualTo("600000100");
                    assertThat(row.getUserSnapshot().getFirstName()).isEqualTo("cliente0");
                    assertThat(row.getSurveyCount()).isGreaterThanOrEqualTo(1);
                    assertThat(row.getSurveyQuestionCount()).isGreaterThanOrEqualTo(2);
                });
        assertThat(report)
                .filteredOn(row -> row.getUserSnapshot().getId().equals(USER_3) && row.getLanguage().equals("English"))
                .singleElement().satisfies(row -> {
                    assertThat(row.getUserSnapshot().getMobile()).isEqualTo("600000103");
                    assertThat(row.getSurveyQuestionCount()).isGreaterThanOrEqualTo(3);
                });
    }

    @Test
    void testCreateDuplicateTitle() {
        CreationSurvey creation = this.creation();
        creation.setTitle("Course satisfaction");
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateUnknownQuestion() {
        CreationSurvey creation = this.creation();
        creation.setSurveyQuestionIds(List.of(QUESTION_0, UNKNOWN_ID));
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateUnknownUser() {
        CreationSurvey creation = this.creation();
        creation.setUserId(UNKNOWN_ID);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.NotFound.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidTitle(String title) {
        CreationSurvey creation = this.creation();
        creation.setTitle(title);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidDescription(String description) {
        CreationSurvey creation = this.creation();
        creation.setDescription(description);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutUser() {
        CreationSurvey creation = this.creation();
        creation.setUserId(null);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutQuestions() {
        CreationSurvey creation = this.creation();
        creation.setSurveyQuestionIds(null);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateEmptyQuestions() {
        CreationSurvey creation = this.creation();
        creation.setSurveyQuestionIds(List.of());
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateNullQuestionId() {
        CreationSurvey creation = this.creation();
        creation.setSurveyQuestionIds(Arrays.asList((UUID) null));
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }
}