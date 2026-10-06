package es.upm.miw.apaw.functionaltests.stucktaskdetector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StuckTaskAlertPatch {
    private String reference;
    private LocalDate resolvedAt;
    private Boolean escalated;
    private String resolutionNotes;
}
