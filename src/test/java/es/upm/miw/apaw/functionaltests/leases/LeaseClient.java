package es.upm.miw.apaw.functionaltests.leases;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "leases", url = "${test.api.apaw-practice}")
public interface LeaseClient {
    @GetMapping("/leases")
    List<Lease> find(@SpringQueryMap LeaseFindCriteria criteria);

    @GetMapping("/leases/report")
    List<LeaseAmendmentReport> findAmendmentReport();

    @PostMapping("/leases")
    Lease create(@RequestBody CreationLease creation);
}
