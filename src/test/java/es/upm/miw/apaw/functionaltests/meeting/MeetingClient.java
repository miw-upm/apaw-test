package es.upm.miw.apaw.functionaltests.meeting;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "meetings", url = "${test.api.apaw-practice}")
public interface MeetingClient {
    @GetMapping("/meetings")
    List<Meeting> find(@SpringQueryMap MeetingFindCriteria criteria);

    @PostMapping("/meetings")
    Meeting create(@RequestBody CreationMeeting creation);
}
