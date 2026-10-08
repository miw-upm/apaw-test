package es.upm.miw.apaw.functionaltests.survey;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "surveyQuestions", url = "${test.api.apaw-practice}")
public interface SurveyQuestionClient {
    @PostMapping("/survey-questions")
    SurveyQuestion create(@RequestBody SurveyQuestion surveyQuestion);

    @GetMapping("/survey-questions")
    List<SurveyQuestion> findAll();

    @GetMapping("/survey-questions/{id}")
    SurveyQuestion read(@PathVariable("id") UUID id);

    @PutMapping("/survey-questions/{id}")
    SurveyQuestion update(@PathVariable("id") UUID id, @RequestBody SurveyQuestion surveyQuestion);

    @PatchMapping("/survey-questions")
    void patchText(@RequestBody List<SurveyQuestionTextPatch> textPatches);

    @DeleteMapping("/survey-questions/{id}")
    void delete(@PathVariable("id") UUID id);
}
