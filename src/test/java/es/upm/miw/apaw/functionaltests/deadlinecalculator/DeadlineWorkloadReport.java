package es.upm.miw.apaw.functionaltests.deadlinecalculator;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DeadlineWorkloadReport {
    private UUID userId;
    private UserSnapshot userSnapshot;
    private long expiredDeadlineCount;
    private long totalDeadlineCount;
    private long holidayAffectedDeadlineCount;
}
