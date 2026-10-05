package es.upm.miw.apaw.functionaltests.training;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "courses", url = "${test.api.apaw-practice}")
public interface CourseClient {
    @PostMapping("/training/courses")
    Course create(@RequestBody Course course);

    @GetMapping("/training/courses/{id}")
    Course read(@PathVariable("id") UUID id);

    @PutMapping("/training/courses/{id}")
    Course update(@PathVariable("id") UUID id, @RequestBody Course course);

    @DeleteMapping("/training/courses/{id}")
    void delete(@PathVariable("id") UUID id);

    @GetMapping("/training/courses")
    List<Course> findAll();

    @PatchMapping("/training/courses")
    void updateDurationHours(@RequestBody List<CourseDurationUpdate> updates);

    @GetMapping("/training/courses/modality-report")
    List<TrainingModalityReport> findModalityReport();
}
