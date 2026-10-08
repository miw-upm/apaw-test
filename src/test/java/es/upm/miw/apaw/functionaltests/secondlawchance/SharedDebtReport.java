package es.upm.miw.apaw.functionaltests.secondlawchance;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SharedDebtReport(
        UUID debtId,
        String contractNumber,
        String creditorName,
        BigDecimal amount,
        CreditorType type,
        long caseCount,
        long debtorCount,
        List<UUID> debtorIds,
        List<UserSnapshot> debtors
) {

}
