package es.upm.miw.apaw.functionaltests.contract;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "clauses", url = "${test.api.apaw-practice}")
public interface ClauseClient {

    @PostMapping("/clauses")
    Clause create(@RequestBody Clause clause);

    @GetMapping("/clauses/{id}")
    Clause read(@PathVariable("id") UUID id);

    @PutMapping("/clauses/{id}")
    Clause update(@PathVariable("id") UUID id,
                  @RequestBody Clause clause);

    @DeleteMapping("/clauses/{id}")
    void delete(@PathVariable("id") UUID id);

    @GetMapping("/clauses")
    List<Clause> findAll();

    @PatchMapping("/clauses/{id}")
    Clause patch(@PathVariable("id") UUID id,
                 @RequestBody ClauseUpdate patch);
}