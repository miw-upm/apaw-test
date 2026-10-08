package es.upm.miw.apaw.functionaltests.immigrationissues;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LawBasis {
    private UUID id;
    private String lawCode;
    private String lawName;
    private Integer articleNumber;
    private LocalDate publishedOn;
    private Boolean active;
}
