package es.upm.miw.apaw.functionaltests.probate;

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
public class Heir {
    private UUID id;
    private String fullName;
    private String nationalId;
    private LocalDate birthDate;
    private BigDecimal sharePercentage;
    private HeirStatus heirStatus;
    private String contactEmail;
}
