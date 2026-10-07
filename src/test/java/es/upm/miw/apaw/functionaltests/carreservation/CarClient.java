package es.upm.miw.apaw.functionaltests.carreservation;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@FeignClient(name = "cars", url = "${test.api.apaw-practice}")
public interface CarClient {
    @PostMapping("/cars")
    Car create(@RequestBody Car car);

    @GetMapping("/cars/{id}")
    Car read(@PathVariable UUID id);

    @PutMapping("/cars/{id}")
    Car update(@PathVariable UUID id, @RequestBody Car car);

    @DeleteMapping("/cars/{id}")
    void delete(@PathVariable UUID id);

    @GetMapping("/cars")
    List<Car> findAll();
}