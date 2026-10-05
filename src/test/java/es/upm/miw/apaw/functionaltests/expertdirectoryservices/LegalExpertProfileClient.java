package es.upm.miw.apaw.functionaltests.expertdirectoryservices;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@FeignClient(name = "legalExpertProfiles", url = "${test.api.apaw-practice}",
        path = "/expert-directory-services/legal-expert-profiles")
public interface LegalExpertProfileClient {
    @PostMapping
    LegalExpertProfile create(@RequestBody LegalExpertProfile profile);

    @GetMapping
    List<LegalExpertProfile> findAll();

    @GetMapping("/report")
    List<LegalExpertProfileSpecialtyReport> findSpecialtyReport();

    @GetMapping("/{id}")
    LegalExpertProfile read(@PathVariable("id") String id);

    @PutMapping("/{id}")
    LegalExpertProfile update(@PathVariable("id") String id, @RequestBody LegalExpertProfile profile);

    @PatchMapping
    void updatePartial(@RequestBody List<LegalExpertProfile> updates);

    @DeleteMapping("/{id}")
    Map<String, String> delete(@PathVariable("id") String id);
}
