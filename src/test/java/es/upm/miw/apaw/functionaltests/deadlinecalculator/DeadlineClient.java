package es.upm.miw.apaw.functionaltests.deadlinecalculator;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "deadlines", url = "${test.api.apaw-practice}")
public interface DeadlineClient {
    @PostMapping("/deadlines")
    Deadline create(@RequestBody CreationDeadline creation);

    @GetMapping("/deadlines")
    List<Deadline> find(@SpringQueryMap DeadlineFindCriteria criteria);

    @GetMapping("/deadlines/report")
    List<DeadlineWorkloadReport> findWorkloadReport();
}
