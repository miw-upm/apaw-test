package es.upm.miw.apaw.functionaltests.stucktaskdetector;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "stuckTaskAlerts", url = "${test.api.apaw-practice}")
public interface StuckTaskAlertClient {
    @PostMapping("/stuck-task-alerts")
    StuckTaskAlert create(@RequestBody StuckTaskAlertCreation creation);

    @GetMapping("/stuck-task-alerts")
    List<StuckTaskAlert> findAll();

    @GetMapping("/stuck-task-alerts/search")
    List<StuckTaskAlert> find(@SpringQueryMap StuckTaskAlertFindCriteria criteria);

    @GetMapping("/stuck-task-alerts/{id}")
    StuckTaskAlert read(@PathVariable("id") UUID id);

    @PutMapping("/stuck-task-alerts/{id}")
    StuckTaskAlert update(@PathVariable("id") UUID id, @RequestBody StuckTaskAlert alert);

    @PatchMapping("/stuck-task-alerts/{id}")
    StuckTaskAlert patch(@PathVariable("id") UUID id, @RequestBody StuckTaskAlertPatch patch);

    @DeleteMapping("/stuck-task-alerts/{id}")
    void delete(@PathVariable("id") UUID id);
}
