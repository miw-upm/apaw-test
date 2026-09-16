package es.upm.miw.apaw.functionaltests.legalprocedure;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "legalTasks", url = "${test.api.apaw-practice}")
public interface LegalTaskClient {
    @PostMapping("/legal-tasks")
    LegalTask create(@RequestBody LegalTask task);

    @GetMapping("/legal-tasks")
    List<LegalTask> findAll();

    @GetMapping("/legal-tasks/report")
    List<LegalTaskUsageReport> findUsageReport();

    @GetMapping("/legal-tasks/{id}")
    LegalTask read(@PathVariable("id") UUID id);

    @PutMapping("/legal-tasks/{id}")
    LegalTask update(@PathVariable("id") UUID id, @RequestBody LegalTask task);

    @PatchMapping("/legal-tasks")
    void updateTaskStatuses(@RequestBody List<LegalTaskStatusUpdate> updates);

    @DeleteMapping("/legal-tasks/{id}")
    void delete(@PathVariable("id") UUID id);
}
