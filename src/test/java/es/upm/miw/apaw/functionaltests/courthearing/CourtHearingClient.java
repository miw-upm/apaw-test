package es.upm.miw.apaw.functionaltests.courthearing;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "courts", url = "${test.api.apaw-practice}")
public interface CourtHearingClient {
    @GetMapping("/courts")
    List<Court> findAllCourts();

    @GetMapping("/courts/{id}")
    Court readCourt(@PathVariable UUID id);

    @PostMapping("/courts")
    Court createCourt(@RequestBody Court court);

    @PutMapping("/courts/{id}")
    Court updateCourt(@PathVariable UUID id, @RequestBody Court court);

    @PatchMapping("/courts/{id}")
    Court patchCourt(@PathVariable UUID id, @RequestBody CourtUpdate courtUpdate);

    @DeleteMapping("/courts/{id}")
    void deleteCourt(@PathVariable UUID id);

    @GetMapping("/courts/report")
    List<CourtHearingByCourtReport> findHearingByCourtReport();

    @GetMapping("/court-hearings")
    List<CourtHearing> findHearings(@SpringQueryMap CourtHearingFindCriteria criteria);

    @PostMapping("/court-hearings")
    CourtHearing createHearing(@RequestBody CreationCourtHearing creation);
}