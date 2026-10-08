package es.upm.miw.apaw.functionaltests.secondlawchance;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreationExonerationCase {
    private String caseNumber;
    private LocalDate resolutionDate;
    private String lawyer;
    private List<UUID> debtIds;
    private UUID userId;
}
