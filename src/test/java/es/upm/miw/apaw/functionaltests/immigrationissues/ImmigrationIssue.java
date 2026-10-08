package es.upm.miw.apaw.functionaltests.immigrationissues;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImmigrationIssue {
    private UUID id;
    private String subject;
    private String clientNationality;
    private String clientImmigrationStatus;
    private LocalDateTime openedAt;
    private LocalDate responseDueDate;
    private BigDecimal estimatedCost;
    private List<LawBasis> lawBases;
    private UserSnapshot userSnapshot;
}
