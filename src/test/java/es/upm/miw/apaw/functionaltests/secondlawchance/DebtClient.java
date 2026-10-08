package es.upm.miw.apaw.functionaltests.secondlawchance;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "debts", url = "${test.api.apaw-practice}")
public interface DebtClient {

    @PostMapping("/debts")
    Debt create(@RequestBody Debt debt);

    @GetMapping("/debts")
    List<Debt> findAll();

    @GetMapping("/debts/report")
    List<SharedDebtReport> findSharedReport();

    @GetMapping("/debts/{id}")
    Debt read(@PathVariable("id") UUID id);

    @PutMapping("/debts/{id}")
    Debt update(@PathVariable("id") UUID id,
                @RequestBody Debt debt);

    @PatchMapping("/debts/{id}")
    Debt patch(@PathVariable("id") UUID id,
               @RequestBody DebtPatch patch);

    @DeleteMapping("/debts/{id}")
    void delete(@PathVariable("id") UUID id);
}
