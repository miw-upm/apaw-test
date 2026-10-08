package es.upm.miw.apaw.functionaltests.credentials;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "verifications", url = "${test.api.apaw-practice}")
public interface VerificationClient {

    @PostMapping("/verifications")
    Verification create(@RequestBody Verification verification);

    @GetMapping("/verifications")
    List<Verification> findAll();

    @GetMapping("/verifications/{id}")
    Verification read(@PathVariable("id") UUID id);

    @PutMapping("/verifications/{id}")
    Verification update(
            @PathVariable("id") UUID id,
            @RequestBody Verification verification);

    @PatchMapping("/verifications/{id}")
    Verification patch(
            @PathVariable("id") UUID id,
            @RequestBody VerificationPatch patch);

    @DeleteMapping("/verifications/{id}")
    void delete(@PathVariable("id") UUID id);
}