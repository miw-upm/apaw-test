package es.upm.miw.apaw.functionaltests.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceFindCriteria {

    private Boolean paid;
    private Integer issueYear;
    private String serviceName;
    private String customerIdentity;
}
