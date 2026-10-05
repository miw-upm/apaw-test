package es.upm.miw.apaw.functionaltests.expertdirectoryservices;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpertServiceScheduleFindCriteria {
    private BigDecimal minRateAmount;
    private Boolean withSpecialCondition;
    private String specialtyArea;
    private String userEmail;
}
