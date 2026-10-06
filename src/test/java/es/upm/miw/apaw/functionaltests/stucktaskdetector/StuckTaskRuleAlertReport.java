package es.upm.miw.apaw.functionaltests.stucktaskdetector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StuckTaskRuleAlertReport {
    private String ruleName;
    private UserSnapshot createdByUser;
    private long totalAlertCount;
    private long unresolvedAlertCount;
}
