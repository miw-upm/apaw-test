package es.upm.miw.apaw.functionaltests.secondlawchance;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "exoneration-cases", url = "${test.api.apaw-practice}")
public interface ExonerationCaseClient {

    @PostMapping("/exoneration-cases")
    ExonerationCase create(@RequestBody CreationExonerationCase creation);

    @GetMapping("/exoneration-cases")
    List<ExonerationCase> find(@SpringQueryMap ExonerationCaseFindCriteria criteria);
}
