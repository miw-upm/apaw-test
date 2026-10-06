package es.upm.miw.apaw.functionaltests.stucktaskdetector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StuckTaskAlert {
    private UUID id;
    private String reference;
    private LocalDate detectedAt;
    private LocalDate resolvedAt;
    private Boolean escalated;
    private String resolutionNotes;
    private StuckTaskRule stuckTaskRule;
}
