package es.upm.miw.apaw.functionaltests.stucktaskdetector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StuckTaskAlertFindCriteria {
    private String procedureKeyword;
    private Boolean withPenalty;
    private Boolean escalated;
    private String creatorEmail;
}
