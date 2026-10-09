package es.upm.miw.apaw.functionaltests.powerofattorney;

import es.upm.miw.apaw.functionaltests.powerofattorney.dto.CreationPowerOfAttorney;
import es.upm.miw.apaw.functionaltests.powerofattorney.model.PowerOfAttorney;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "powerOfAttorney", url = "${test.api.apaw-practice}")
public interface PowerOfAttorneyClient {
    @PostMapping("/power-of-attorneys")
    PowerOfAttorney create(@RequestBody CreationPowerOfAttorney creation);
}
