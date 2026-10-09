package es.upm.miw.apaw.functionaltests.credentials;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "credentials", url = "${test.api.apaw-practice}")
public interface CredentialClient {

    @PostMapping("/credentials")
    Credential create(@RequestBody CreationCredential creation);

    @GetMapping("/credentials")
    List<Credential> find(@SpringQueryMap CredentialFindCriteria criteria);

    @GetMapping("/credentials/report")
    List<CredentialVerificationReport> findVerificationReport();

    @GetMapping("/credentials/{id}")
    Credential read(@PathVariable("id") UUID id);
}