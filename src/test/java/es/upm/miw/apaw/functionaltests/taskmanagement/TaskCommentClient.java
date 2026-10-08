package es.upm.miw.apaw.functionaltests.taskmanagement;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "taskComments", url = "${test.api.apaw-practice}")
public interface TaskCommentClient {

    @PostMapping("/task-comments")
    TaskComment create(@RequestBody TaskComment taskComment);

    @GetMapping("/task-comments")
    List<TaskComment> findAll();

    @GetMapping("/task-comments/{id}")
    TaskComment read(@PathVariable("id") UUID id);

    @PutMapping("/task-comments/{id}")
    TaskComment update(@PathVariable("id") UUID id, @RequestBody TaskComment taskComment);

    @DeleteMapping("/task-comments/{id}")
    void delete(@PathVariable("id") UUID id);
}
