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
public class CreationEvidence {
    private String title;
    private String description;
    private EvidenceType evidenceType;
    private LocalDateTime collectionDate;
    private String source;
    private Boolean confidential;
    private List<UUID> custodyRecordIds;
}