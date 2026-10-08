package es.upm.miw.apaw.functionaltests.probate;

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
public class Estate {
    private UUID id;
    private String fileNumber;
    private LocalDate openedDate;
    private String deceasedName;
    private BigDecimal netValue;
    private Boolean lastWill;
    private LocalDate closingDate;
    private List<Heir> heirs;
    private UserSnapshot userSnapshot;
}
