package es.upm.miw.apaw.functionaltests.stucktaskdetector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StuckTaskRuleCreation {
    private String name;
    private String procedureKeyword;
    private Integer thresholdDays;
    private BigDecimal penaltyAmount;
    private Boolean active;
    private UUID userId;
}
