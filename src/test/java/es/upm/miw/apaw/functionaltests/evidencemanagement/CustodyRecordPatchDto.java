package es.upm.miw.apaw.functionaltests.evidencemanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustodyRecordPatchDto {
    private Integer durationMinutes;
    private String action;
    private String location;
    private String notes;
    private UUID custodianId;
}
