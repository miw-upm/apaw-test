package es.upm.miw.apaw.functionaltests.expense;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Expense {
    private UUID id;
    private String reference;
    private BigDecimal amount;
    private String description;
    private LocalDate expenseDate;
    private String category;
    private Boolean isPaid;
    private Supplier supplier;
    private UserSnapshot userSnapshot;
}