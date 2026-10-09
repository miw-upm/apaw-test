package es.upm.miw.apaw.functionaltests.evidencemanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustodyRecord {
    private UUID id;
    private LocalDateTime recordedAt;
    private Integer durationMinutes;
    private String action;
    private String location;
    private String notes;
    private UserSnapshot custodian;
}
