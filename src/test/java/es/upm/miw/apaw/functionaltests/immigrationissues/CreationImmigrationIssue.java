package es.upm.miw.apaw.functionaltests.immigrationissues;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreationImmigrationIssue {
    private String subject;
    private String clientNationality;
    private String clientImmigrationStatus;
    private LocalDate responseDueDate;
    private BigDecimal estimatedCost;
    private List<UUID> lawBasisIds;
    private UUID userId;
}
