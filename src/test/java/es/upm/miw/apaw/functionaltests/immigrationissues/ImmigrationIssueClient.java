package es.upm.miw.apaw.functionaltests.immigrationissues;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "immigrationIssues", url = "${test.api.apaw-practice}")
public interface ImmigrationIssueClient {
    @GetMapping("/immigration-issues")
    List<ImmigrationIssue> find(@SpringQueryMap ImmigrationIssueFindCriteria criteria);

    @PostMapping("/immigration-issues")
    ImmigrationIssue create(@RequestBody CreationImmigrationIssue creation);

    @GetMapping("/immigration-issues/report")
    List<LawBasisUsageReport> findUsageReport();
}
