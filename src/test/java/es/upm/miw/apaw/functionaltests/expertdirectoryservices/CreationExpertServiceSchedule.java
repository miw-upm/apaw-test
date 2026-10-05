package es.upm.miw.apaw.functionaltests.expertdirectoryservices;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreationExpertServiceSchedule {
    private String tariffCode;
    private String description;
    private BigDecimal rateAmount;
    private String currency;
    private String specialCondition;
    private List<UUID> legalExpertProfileIds;
}
