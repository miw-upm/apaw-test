package es.upm.miw.apaw.functionaltests.expense;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "suppliers", url = "${test.api.apaw-practice}")
public interface SupplierClient {

    @PostMapping("/expense/suppliers")
    Supplier create(@RequestBody Supplier supplier);

    @GetMapping("/expense/suppliers/{id}")
    Supplier read(@PathVariable("id") UUID id);

    @PutMapping("/expense/suppliers/{id}")
    Supplier update(@PathVariable("id") UUID id, @RequestBody Supplier supplier);

    @DeleteMapping("/expense/suppliers/{id}")
    void delete(@PathVariable("id") UUID id);

    @GetMapping("/expense/suppliers")
    List<Supplier> findAll();

    @PatchMapping("/expense/suppliers/{id}")
    Supplier patch(@PathVariable("id") UUID id, @RequestBody Supplier supplier);

    @GetMapping("/expense/suppliers/report")
    List<SupplierExpenseReport> findExpenseReport();
}