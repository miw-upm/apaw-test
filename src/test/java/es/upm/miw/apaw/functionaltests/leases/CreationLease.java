package es.upm.miw.apaw.functionaltests.leases;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreationLease {
    private String leaseNumber;
    private String cadastralReference;
    private String propertyAddress;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal monthlyRent;
    private BigDecimal deposit;
    private Boolean active;
    private LeaseType leaseType;
    private List<UUID> amendmentIds;
    private UUID userId;
}
