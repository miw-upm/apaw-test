package es.upm.miw.apaw.functionaltests.training;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;
import org.junit.jupiter.api.Test;

import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = TrainingPlanResourceFT.ApiTestConfig.class)
@ActiveProfiles("test")
class TrainingPlanResourceFT {

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = {TrainingPlanClient.class, CourseClient.class})
    static class ApiTestConfig {
    }

    @Autowired
    private TrainingPlanClient trainingPlanClient;

    @Autowired
    private CourseClient courseClient;

    @Test
    void testCreate() {
        List<Course> courses = this.courseClient.findAll();
        CreationTrainingPlan creation = CreationTrainingPlan.builder()
                .planCode("FT-PLAN-" + UUID.randomUUID())
                .evaluationScore(new BigDecimal("9.5"))
                .courseIds(List.of(courses.get(0).getId()))
                .userIds(List.of(UUID.fromString("11111111-2222-3333-4444-555555550001")))
                .build();
        TrainingPlan created = this.trainingPlanClient.create(creation);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getPlanCode()).isEqualTo(creation.getPlanCode());
    }

    @Test
    void testFind() {
        TrainingPlanFindCriteria criteria = TrainingPlanFindCriteria.builder()
                .courseName("Angular")
                .build();
        List<TrainingPlan> plans = this.trainingPlanClient.find(criteria);
        assertThat(plans).isNotEmpty();
    }
}
