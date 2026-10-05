package es.upm.miw.apaw.functionaltests.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrainingPlan {
    private UUID id;
    private String planCode;
    private LocalDate approvalDate;
    private LocalDate endDate;
    private BigDecimal evaluationScore;
    private List<Course> courses;
    private List<UserSnapshot> userSnapshots;
}
