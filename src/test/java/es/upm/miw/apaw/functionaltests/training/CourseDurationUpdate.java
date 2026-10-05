package es.upm.miw.apaw.functionaltests.training;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourseDurationUpdate {
    private UUID id;
    private Integer durationHours;
}
