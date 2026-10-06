package es.upm.miw.apaw.functionaltests.stucktaskdetector;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "stuckTaskRules", url = "${test.api.apaw-practice}")
public interface StuckTaskRuleClient {
    @PostMapping("/stuck-task-rules")
    StuckTaskRule create(@RequestBody StuckTaskRuleCreation creation);

    @GetMapping("/stuck-task-rules/report")
    List<StuckTaskRuleAlertReport> findAlertReport();
}
