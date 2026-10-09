package es.upm.miw.apaw.functionaltests.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreationInvoice {

    private BigDecimal vatRate;
    private List<UUID> legalServiceIds;
    private UUID userId;

}
