package es.upm.miw.apaw.functionaltests.deadlinecalculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeadlineFindCriteria {
    private String region;
    private Boolean overdue;
    private ScopeLevel scopeLevel;
    private String userMobile;
}
