package es.upm.miw.apaw.functionaltests.meeting;

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
public class LegalIssue {
    private UUID id;
    private String title;
    private String description;
    private Integer priority;
    private Boolean resolved;
    private LocalDateTime creationDate;
}
