package es.upm.miw.apaw.functionaltests.contract;

import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Clause {
    private UUID id;
    private String title;
    private ClauseType type;
    private String content;
    private LocalDate effectiveFrom;
    private LocalDate effectiveUntil;
    private String notes;
    private Integer version;
}