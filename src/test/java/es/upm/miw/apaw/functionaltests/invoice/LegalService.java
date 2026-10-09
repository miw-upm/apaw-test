package es.upm.miw.apaw.functionaltests.invoice;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LegalService {

    private UUID id;
    private String name;
    private String description;
    private BigDecimal fee;
    private Boolean requiresAppointment;
    private ServiceCategory category;
    private LegalArea legalArea;

}
