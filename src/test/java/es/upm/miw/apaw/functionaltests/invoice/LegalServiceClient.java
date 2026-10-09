package es.upm.miw.apaw.functionaltests.invoice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(
        name = "legalServices",
        url = "${test.api.apaw-practice}"
)
public interface LegalServiceClient {

    @GetMapping("/legal-services/{id}")
    LegalService findById(@PathVariable("id") UUID id);

}
