package es.upm.miw.apaw.functionaltests.judicialcourt;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "judicialcourttype-client", url = "${test.api.apaw-practice}/judicial-court-types")
public interface JudicialCourtTypeClient {

    @PostMapping
    JudicialCourtType create(@RequestBody JudicialCourtType judicialCourtType);

    @GetMapping
    List<JudicialCourtType> findAll();

    @GetMapping("/{id}")
    JudicialCourtType read(@PathVariable UUID id);

    @PutMapping("/{id}")
    JudicialCourtType update(@PathVariable UUID id, @RequestBody JudicialCourtType judicialCourtType);

    @PatchMapping("/{id}")
    JudicialCourtType patch(@PathVariable UUID id, @RequestBody JudicialCourtTypeUpdate update);

    @DeleteMapping("/{id}")
    void delete(@PathVariable UUID id);
}
