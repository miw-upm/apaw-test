package es.upm.miw.apaw.functionaltests.judicialcourt;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "judicialcourt-client", url = "${test.api.apaw-practice}/judicial-courts")
public interface
JudicialCourtClient {

    @PostMapping
    JudicialCourt create(@RequestBody CreationJudicialCourt creation);

    @GetMapping
    List<JudicialCourt> find(@RequestParam(required = false) String city,
                             @RequestParam(required = false) Boolean completeContactInformation,
                             @RequestParam(required = false) String jurisdiction,
                             @RequestParam(required = false) String userIdentity);

    @GetMapping("/lawyers-ranking")
    List<LawyerCourtRankingReport> ranking();
}
