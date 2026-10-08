package es.upm.miw.apaw.functionaltests.survey;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "surveys", url = "${test.api.apaw-practice}")
public interface SurveyClient {
    @GetMapping("/surveys")
    List<Survey> find(@SpringQueryMap SurveyFindCriteria criteria);

    @GetMapping("/surveys/report")
    List<SurveyUserLanguageReport> findUserLanguageReport();

    @PostMapping("/surveys")
    Survey create(@RequestBody CreationSurvey creation);
}
