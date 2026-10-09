package es.upm.miw.apaw.functionaltests.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalServiceInvoiceReport {

    private String serviceName;
    private Long totalInvoiceCount;
    private Long paidInvoiceCount;
}
