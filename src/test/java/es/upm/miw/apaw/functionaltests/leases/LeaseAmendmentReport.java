package es.upm.miw.apaw.functionaltests.leases;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaseAmendmentReport {
    private LeaseType leaseType;
    private long leaseCount;
    private long approvedAmendmentCount;
    private BigDecimal totalAdditionalAmount;
}
