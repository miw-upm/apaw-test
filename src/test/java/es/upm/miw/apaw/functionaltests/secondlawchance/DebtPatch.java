package es.upm.miw.apaw.functionaltests.secondlawchance;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DebtPatch(
        String contractNumber,
        LocalDate issueDate,
        String creditorName,
        BigDecimal amount,
        CreditorType type,
        Boolean guarantee
) {

}
