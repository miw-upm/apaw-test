package es.upm.miw.apaw.functionaltests.notifications;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "notificationTemplates", url = "${test.api.apaw-practice}")
public interface NotificationTemplateClient {
    @GetMapping("/notification-template")
    List<NotificationTemplate> findAll();

    @GetMapping("/notification-template/{id}")
    NotificationTemplate read(@PathVariable("id") UUID id);

    @PostMapping("/notification-template")
    NotificationTemplate create(@RequestBody NotificationTemplate template);

    @PutMapping("/notification-template/{id}")
    NotificationTemplate update(@PathVariable("id") UUID id, @RequestBody NotificationTemplate template);

    @PatchMapping("/notification-template/{id}")
    NotificationTemplate patch(@PathVariable("id") UUID id, @RequestBody NotificationTemplate template);

    @DeleteMapping("/notification-template/{id}")
    void delete(@PathVariable("id") UUID id);
}
