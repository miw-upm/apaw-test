package es.upm.miw.apaw.functionaltests.secondlawchance;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Debt {
    private UUID id;
    private String contractNumber;
    private LocalDate issueDate;
    private String creditorName;
    private BigDecimal amount;
    private CreditorType type;
    private Boolean guarantee;
}
