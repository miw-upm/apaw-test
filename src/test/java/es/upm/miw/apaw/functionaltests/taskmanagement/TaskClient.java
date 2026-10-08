package es.upm.miw.apaw.functionaltests.taskmanagement;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "tasks", url = "${test.api.apaw-practice}")
public interface TaskClient {

    @PostMapping("/tasks")
    Task create(@RequestBody CreationTask creation);

    @GetMapping("/tasks")
    List<Task> find(@SpringQueryMap TaskFindCriteria criteria);

    @GetMapping("/tasks/report")
    List<TaskActivityReport> findActivityReport();
}