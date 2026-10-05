package es.upm.miw.apaw.functionaltests.training;

import es.upm.miw.apaw.functionaltests.ApiTestConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = ApiTestConfig.class)
class CourseResourceFT {

    @Autowired
    private CourseClient courseClient;

    @Test
    void testCreate() {
        Course course = Course.builder()
                .name("New Test Course " + UUID.randomUUID())
                .certificateReference("Ref-FT-1")
                .durationHours(40)
                .online(true)
                .launchDate(LocalDate.now())
                .build();
        Course created = this.courseClient.create(course);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo(course.getName());
    }

    @Test
    void testRead() {
        List<Course> courses = this.courseClient.findAll();
        assertThat(courses).isNotEmpty();
        Course readCourse = this.courseClient.read(courses.get(0).getId());
        assertThat(readCourse.getName()).isEqualTo(courses.get(0).getName());
    }

    @Test
    void testUpdate() {
        List<Course> courses = this.courseClient.findAll();
        Course toUpdate = courses.get(0);
        toUpdate.setDurationHours(toUpdate.getDurationHours() + 5);
        Course updated = this.courseClient.update(toUpdate.getId(), toUpdate);
        assertThat(updated.getDurationHours()).isEqualTo(toUpdate.getDurationHours());
    }

    @Test
    void testPatch() {
        List<Course> courses = this.courseClient.findAll();
        Course toPatch = courses.get(1);
        int newDuration = toPatch.getDurationHours() + 10;
        this.courseClient.updateDurationHours(List.of(new CourseDurationUpdate(toPatch.getId(), newDuration)));
        Course patched = this.courseClient.read(toPatch.getId());
        
        assertThat(patched.getDurationHours()).isEqualTo(newDuration);
    }

    @Test
    void testReport() {
        List<TrainingModalityReport> report = this.courseClient.findModalityReport();
        assertThat(report).isNotEmpty();
    }
    
    @Test
    void testDelete() {
        List<Course> courses = this.courseClient.findAll();
        this.courseClient.delete(courses.get(2).getId()); 
    }
}
