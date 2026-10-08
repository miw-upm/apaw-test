package es.upm.miw.apaw.functionaltests.euregulation;

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

@FeignClient(name = "euRegulations", url = "${test.api.apaw-practice}")
public interface EURegulationClient {
    @PostMapping("/eu-regulations")
    EURegulation create(@RequestBody CreationEURegulation creation);

    @GetMapping("/eu-regulations")
    List<EURegulation> findAll();

    @GetMapping("/eu-regulations/{id}")
    EURegulation read(@PathVariable("id") UUID id);

    @PutMapping("/eu-regulations/{id}")
    EURegulation update(@PathVariable("id") UUID id, @RequestBody CreationEURegulation update);

    @PatchMapping("/eu-regulations/{id}")
    EURegulation patch(@PathVariable("id") UUID id, @RequestBody EURegulationPatch patch);

    @DeleteMapping("/eu-regulations/{id}")
    void delete(@PathVariable("id") UUID id);
}
