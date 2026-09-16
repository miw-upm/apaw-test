package es.upm.miw.apaw.functionaltests.legalprocedure;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegalTask {
    private UUID id;
    private String title;
    private LocalDateTime creatingDate;
    private String notes;
    private TaskStatus taskStatus;
}
