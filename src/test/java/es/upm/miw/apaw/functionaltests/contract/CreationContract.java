package es.upm.miw.apaw.functionaltests.contract;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreationContract {
    private String title;
    private ContractType type;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal amount;
    private Boolean automaticRenewal;
    private List<UUID> clauseIds;
    private UUID userId;
}