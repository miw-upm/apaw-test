package es.upm.miw.apaw.functionaltests.training;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreationTrainingPlan {
    private String planCode;
    private BigDecimal evaluationScore;
    private List<UUID> coursesIds;
    private List<UUID> userIds;
}
