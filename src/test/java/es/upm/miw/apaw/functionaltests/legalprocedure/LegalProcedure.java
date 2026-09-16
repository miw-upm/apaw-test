package es.upm.miw.apaw.functionaltests.legalprocedure;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;
import java.util.List;
import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegalProcedure {
    private UUID id;
    private String title;
    private LocalDate startedDate;
    private LocalDate closingDate;
    private BigDecimal budget;
    private String budgetProposal;
    private Boolean vatIncluded;
    private List<LegalTask> legalTasks;
    private UserSnapshot userSnapshot;
}
