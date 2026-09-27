package es.upm.miw.apaw.functionaltests.leases;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AmendmentUpdate {
    private Integer amendmentNumber;
    private String description;
    private LocalDate effectiveDate;
    private BigDecimal additionalAmount;
    private Boolean approved;
    private AmendmentType amendmentType;
}
