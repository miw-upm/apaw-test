package es.upm.miw.apaw.functionaltests.expertdirectoryservices;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpertServiceSchedule {
    private UUID id;
    private String tariffCode;
    private String description;
    private BigDecimal rateAmount;
    private String currency;
    private String specialCondition;
    private LocalDate creationDate;
    private List<LegalExpertProfile> legalExpertProfiles;
}
