package es.upm.miw.apaw.functionaltests.evidencemanagement;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "custodyRecords", url = "${test.api.apaw-practice}")
public interface CustodyRecordClient {
    @PostMapping("/custody-records")
    CustodyRecord create(@RequestBody CustodyRecordDto custodyRecordDto);

    @GetMapping("/custody-records")
    List<CustodyRecord> findAll();

    @GetMapping("/custody-records/report")
    List<CustodianActivityReport> findActivityReport();

    @GetMapping("/custody-records/{id}")
    CustodyRecord read(@PathVariable("id") UUID id);

    @PutMapping("/custody-records/{id}")
    CustodyRecord update(@PathVariable("id") UUID id, @RequestBody CustodyRecordDto custodyRecordDto);

    @PatchMapping("/custody-records/{id}")
    CustodyRecord patch(@PathVariable("id") UUID id, @RequestBody CustodyRecordPatchDto custodyRecordPatchDto);

    @DeleteMapping("/custody-records/{id}")
    void delete(@PathVariable("id") UUID id);
}