package es.upm.miw.apaw.functionaltests.courthearing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourtHearingByCourtReport {
    private String courtName;
    private Long totalHearingCount;
    private Long scheduledHearingCount;
}