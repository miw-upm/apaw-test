package es.upm.miw.apaw.functionaltests.meeting;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "legalIssues", url = "${test.api.apaw-practice}")
public interface LegalIssueClient {
    @PostMapping("/legal-issues")
    LegalIssue create(@RequestBody LegalIssue legalIssue);

    @GetMapping("/legal-issues")
    List<LegalIssue> findAll();

    @GetMapping("/legal-issues/report")
    List<MeetingParticipantReport> findParticipantReport();

    @GetMapping("/legal-issues/{id}")
    LegalIssue read(@PathVariable("id") UUID id);

    @PutMapping("/legal-issues/{id}")
    LegalIssue update(@PathVariable("id") UUID id, @RequestBody LegalIssue legalIssue);

    @DeleteMapping("/legal-issues/{id}")
    void delete(@PathVariable("id") UUID id);
}
