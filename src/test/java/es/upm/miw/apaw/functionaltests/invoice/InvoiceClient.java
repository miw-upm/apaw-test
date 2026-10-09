package es.upm.miw.apaw.functionaltests.invoice;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "invoices",
        url = "${test.api.apaw-practice}"
)
public interface InvoiceClient {

    @PostMapping("/invoices")
    Invoice create(@RequestBody CreationInvoice creation);

    @GetMapping("/invoices/findCriteria")
    List<Invoice> findByCriteria(
            @SpringQueryMap InvoiceFindCriteria criteria
    );

    @GetMapping("/invoices/report")
    List<LegalServiceInvoiceReport> findLegalServiceInvoiceReport();

}
