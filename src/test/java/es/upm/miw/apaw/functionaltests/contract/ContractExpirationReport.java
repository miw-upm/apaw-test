package es.upm.miw.apaw.functionaltests.contract;

import java.util.UUID;

public record ContractExpirationReport (
    UUID userId,
    UserSnapshot userSnapshot,
    long expiringContractCount,
    long activeClauseCount
) {
    public ContractExpirationReport(
            UUID userId,
            long expiringContractCount,
            long activeClauseCount
    ) {
        this(
                userId,
                null,
                expiringContractCount,
                activeClauseCount
        );
    }
}
