package es.upm.miw.apaw.functionaltests.legalprocedure;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "legalProcedures", url = "${test.api.apaw-practice}")
public interface LegalProcedureClient {
    @GetMapping("/legal-procedures")
    List<LegalProcedure> find(@SpringQueryMap LegalProcedureFindCriteria criteria);

    @PostMapping("/legal-procedures")
    LegalProcedure create(@RequestBody CreationLegalProcedure creation);
}
