package es.upm.miw.apaw.functionaltests.appointment;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "appointments", url = "${test.api.apaw-practice}")
public interface AppointmentClient {

    @PostMapping("/appointments")
    Appointment create(@RequestBody CreationAppointment creation);

    @GetMapping("/appointments")
    List<Appointment> find(@SpringQueryMap AppointmentFindCriteria criteria);

    @GetMapping("/appointments/report")
    List<AppointmentCityReport> findCityReport();
}