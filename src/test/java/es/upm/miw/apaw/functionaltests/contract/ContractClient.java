package es.upm.miw.apaw.functionaltests.contract;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "contracts", url = "${test.api.apaw-practice}")
public interface ContractClient {

    @PostMapping("/contracts")
    Contract create(@RequestBody CreationContract creation);

    @GetMapping("/contracts/report")
    List<ContractExpirationReport> findExpirationReport();

    @GetMapping("/contracts")
    List<Contract> find(@SpringQueryMap ContractFindCriteria criteria);
}