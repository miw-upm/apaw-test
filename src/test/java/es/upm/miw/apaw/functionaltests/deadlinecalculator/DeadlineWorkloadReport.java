package es.upm.miw.apaw.functionaltests.deadlinecalculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeadlineWorkloadReport {
    private UserSnapshot lawyer;
    private long expiredDeadlineCount;
    private long totalDeadlineCount;
    private long holidayAffectedDeadlineCount;
}
