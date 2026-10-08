package es.upm.miw.apaw.functionaltests.probate;

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

@FeignClient(name = "heirs", url = "${test.api.apaw-practice}")
public interface HeirClient {
    @PostMapping("/heirs")
    Heir create(@RequestBody Heir heir);

    @GetMapping("/heirs")
    List<Heir> list();

    @GetMapping("/heirs/{id}")
    Heir get(@PathVariable("id") UUID id);

    @PutMapping("/heirs/{id}")
    Heir update(@PathVariable("id") UUID id, @RequestBody Heir heir);

    @PatchMapping("/heirs/{id}")
    Heir patch(@PathVariable("id") UUID id, @RequestBody HeirUpdate update);

    @DeleteMapping("/heirs/{id}")
    void delete(@PathVariable("id") UUID id);
}
