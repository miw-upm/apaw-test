package es.upm.miw.apaw.functionaltests.judicialcourt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LawyerCourtRankingReport {
    private UserSnapshot lawyer;
    private Long totalJudicialCourts;
}
