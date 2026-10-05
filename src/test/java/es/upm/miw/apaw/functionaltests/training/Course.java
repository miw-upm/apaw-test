package es.upm.miw.apaw.functionaltests.training;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Course {
    private UUID id;
    private String name;
    private String certificateReference;
    private Integer durationHours;
    private Boolean online;
    private LocalDate launchDate;
}
