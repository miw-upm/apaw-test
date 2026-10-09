package es.upm.miw.apaw.functionaltests.powerofattorney;

import es.upm.miw.apaw.functionaltests.powerofattorney.dto.CreationPowerOfAttorneyParty;
import es.upm.miw.apaw.functionaltests.powerofattorney.dto.PowerOfAttorneyPartyPatch;
import es.upm.miw.apaw.functionaltests.powerofattorney.model.PowerOfAttorneyParty;
import es.upm.miw.apaw.functionaltests.powerofattorney.model.PowerOfAttorneyPartyReport;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "powerOfAttorneyParty", url = "${test.api.apaw-practice}")
public interface PowerOfAttorneyPartyClient {
    @PostMapping("/power-of-attorney-parties")
    PowerOfAttorneyParty create(@RequestBody CreationPowerOfAttorneyParty party);

    @GetMapping("/power-of-attorney-parties")
    List<PowerOfAttorneyParty> findAll();

    @GetMapping("/power-of-attorney-parties/report")
    List<PowerOfAttorneyPartyReport> findReport();

    @GetMapping("/power-of-attorney-parties/{id}")
    PowerOfAttorneyParty read(@PathVariable("id") UUID id);

    @PutMapping("/power-of-attorney-parties/{id}")
    PowerOfAttorneyParty update(@PathVariable("id") UUID id, @RequestBody CreationPowerOfAttorneyParty party);

    @DeleteMapping("/power-of-attorney-parties/{id}")
    void delete(@PathVariable("id") UUID id);

    @PatchMapping("/power-of-attorney-parties")
    void patch(@RequestBody List<PowerOfAttorneyPartyPatch> patches);
}
