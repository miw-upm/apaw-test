package es.upm.miw.apaw.functionaltests.expertdirectoryservices;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "expertServiceSchedules", url = "${test.api.apaw-practice}",
        path = "/expert-directory-services/expert-service-schedules")
public interface ExpertServiceScheduleClient {
    @PostMapping
    ExpertServiceSchedule create(@RequestBody CreationExpertServiceSchedule creation);

    @GetMapping
    List<ExpertServiceSchedule> find(@SpringQueryMap ExpertServiceScheduleFindCriteria criteria);
}
