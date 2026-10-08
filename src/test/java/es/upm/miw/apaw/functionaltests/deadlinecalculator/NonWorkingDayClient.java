package es.upm.miw.apaw.functionaltests.deadlinecalculator;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "nonWorkingDays", url = "${test.api.apaw-practice}")
public interface NonWorkingDayClient {
    @PostMapping("/non-working-days")
    NonWorkingDay create(@RequestBody NonWorkingDay nonWorkingDay);

    @GetMapping("/non-working-days/{id}")
    NonWorkingDay read(@PathVariable("id") UUID id);

    @GetMapping("/non-working-days")
    List<NonWorkingDay> findAll();

    @PutMapping("/non-working-days/{id}")
    NonWorkingDay update(@PathVariable("id") UUID id, @RequestBody NonWorkingDay nonWorkingDay);

    @PatchMapping("/non-working-days")
    void updateRecurrences(@RequestBody List<NonWorkingDayRecurringUpdate> updates);

    @DeleteMapping("/non-working-days/{id}")
    void delete(@PathVariable("id") UUID id);
}
