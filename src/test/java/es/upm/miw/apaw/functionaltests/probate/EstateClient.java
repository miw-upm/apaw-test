package es.upm.miw.apaw.functionaltests.probate;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "estates", url = "${test.api.apaw-practice}")
public interface EstateClient {
    @PostMapping("/estates")
    Estate create(@RequestBody CreationEstate creation);

    @GetMapping("/estates")
    List<Estate> search(@SpringQueryMap EstateFindCriteria criteria);

    @GetMapping("/estates/report")
    List<EstateUsageReport> usageReport();
}
