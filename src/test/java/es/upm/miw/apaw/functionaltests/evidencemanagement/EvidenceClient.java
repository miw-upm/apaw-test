package es.upm.miw.apaw.functionaltests.evidencemanagement;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "evidences", url = "${test.api.apaw-practice}")
public interface EvidenceClient {
    @GetMapping("/evidences")
    List<Evidence> find(@SpringQueryMap EvidenceFindCriteria criteria);

    @PostMapping("/evidences")
    Evidence create(@RequestBody CreationEvidence creation);
}