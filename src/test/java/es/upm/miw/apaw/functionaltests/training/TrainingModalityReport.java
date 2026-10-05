package es.upm.miw.apaw.functionaltests.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingModalityReport {
    private Boolean online;
    private Long planCount;
    private Long totalDurationHours;
}
