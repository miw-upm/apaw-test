package es.upm.miw.apaw.functionaltests.notifications;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "notifications", url = "${test.api.apaw-practice}")
public interface NotificationClient {
    @GetMapping("/notifications")
    List<Notification> find(@SpringQueryMap NotificationFindCriteria criteria);

    @GetMapping("/notifications/report/template-failures")
    List<NotificationTemplateFailureReport> findTemplateFailureReport();

    @PostMapping("/notifications")
    Notification create(@RequestBody CreationNotification creation);
}
