package es.upm.miw.apaw.functionaltests.expense;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreationExpense {
    private String reference;
    private BigDecimal amount;
    private String description;
    private String category;
    private UUID supplierId;
    private UUID applicantId;
}