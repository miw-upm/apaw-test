package es.upm.miw.apaw.functionaltests.carreservation;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "reservations", url = "${test.api.apaw-practice}")
public interface ReservationClient {
    @PostMapping("/reservations")
    Reservation create(@RequestBody CreationReservation creationReservation);

    @GetMapping("/reservations/report")
    List<CarUsageReport> findCarUsageReport();

    @GetMapping("/reservations")
    List<Reservation> find(@SpringQueryMap ReservationFindCriteria criteria);
}