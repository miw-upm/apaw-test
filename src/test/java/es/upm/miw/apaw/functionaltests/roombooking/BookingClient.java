package es.upm.miw.apaw.functionaltests.roombooking;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "bookings", url = "${test.api.apaw-practice}")
public interface BookingClient {
    @GetMapping("/room-booking/bookings")
    List<Booking> find(@SpringQueryMap BookingFindCriteria criteria);

    @PostMapping("/room-booking/bookings")
    Booking create(@RequestBody CreationBooking creation);

    @GetMapping("/room-booking/bookings/report")
    List<UserBookingReport> findUserBookingReports();
}