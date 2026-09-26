package es.upm.miw.apaw.functionaltests.appointment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "appointmentLocations", url = "${test.api.apaw-practice}")
public interface AppointmentLocationClient {

    @PostMapping("/appointment-locations")
    AppointmentLocation create(@RequestBody AppointmentLocation location);

    @GetMapping("/appointment-locations")
    List<AppointmentLocation> findAll();

    @GetMapping("/appointment-locations/{id}")
    AppointmentLocation read(@PathVariable("id") UUID id);

    @PutMapping("/appointment-locations/{id}")
    AppointmentLocation update(@PathVariable("id") UUID id, @RequestBody AppointmentLocation location);

    @PatchMapping("/appointment-locations/{id}")
    AppointmentLocation patch(@PathVariable("id") UUID id, @RequestBody AppointmentLocationPatch patch);

    @DeleteMapping("/appointment-locations/{id}")
    void delete(@PathVariable("id") UUID id);
}