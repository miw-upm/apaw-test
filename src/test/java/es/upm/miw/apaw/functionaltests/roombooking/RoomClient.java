package es.upm.miw.apaw.functionaltests.roombooking;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "rooms", url = "${test.api.apaw-practice}")
public interface RoomClient {
    @PostMapping("/room-booking/rooms")
    Room create(@RequestBody Room room);

    @GetMapping("/room-booking/rooms")
    List<Room> findAll();

    @GetMapping("/room-booking/rooms/{id}")
    Room read(@PathVariable("id") UUID id);

    @PutMapping("/room-booking/rooms/{id}")
    Room update(@PathVariable("id") UUID id, @RequestBody Room room);

    @PatchMapping("/room-booking/rooms/{id}")
    Room patch(@PathVariable("id") UUID id, @RequestBody Room room);

    @DeleteMapping("/room-booking/rooms/{id}")
    void delete(@PathVariable("id") UUID id);
}