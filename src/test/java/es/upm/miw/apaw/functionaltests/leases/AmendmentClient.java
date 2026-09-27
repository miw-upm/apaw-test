package es.upm.miw.apaw.functionaltests.leases;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "amendments", url = "${test.api.apaw-practice}")
public interface AmendmentClient {
    @PostMapping("/amendments")
    Amendment create(@RequestBody Amendment amendment);

    @GetMapping("/amendments")
    List<Amendment> findAll();

    @GetMapping("/amendments/{id}")
    Amendment read(@PathVariable("id") UUID id);

    @PutMapping("/amendments/{id}")
    Amendment update(@PathVariable("id") UUID id, @RequestBody Amendment amendment);

    @PatchMapping("/amendments/{id}")
    Amendment patch(@PathVariable("id") UUID id, @RequestBody AmendmentUpdate update);

    @DeleteMapping("/amendments/{id}")
    void delete(@PathVariable("id") UUID id);
}
