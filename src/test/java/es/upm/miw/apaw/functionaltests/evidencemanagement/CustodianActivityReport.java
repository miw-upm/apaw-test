package es.upm.miw.apaw.functionaltests.evidencemanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustodianActivityReport {
    private UserSnapshot custodian;
    private long recordsCount;
    private long evidencesCount;
    private long totalDurationMinutes;
}