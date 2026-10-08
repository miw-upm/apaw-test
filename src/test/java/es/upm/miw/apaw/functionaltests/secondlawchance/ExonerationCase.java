package es.upm.miw.apaw.functionaltests.secondlawchance;

import lombok.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExonerationCase {
    private UUID id;
    private String caseNumber;
    private LocalDate filingDate;
    private LocalDate resolutionDate;
    private String lawyer;
    private List<Debt> debts;
    private UserSnapshot userSnapshot;
}
