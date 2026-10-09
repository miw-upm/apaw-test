package es.upm.miw.apaw.functionaltests.evidencemanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Evidence {
    private UUID id;
    private String title;
    private String description;
    private EvidenceType evidenceType;
    private EvidenceStatus status;
    private LocalDateTime collectionDate;
    private String source;
    private Boolean confidential;
    private List<CustodyRecord> custodyRecords;
}