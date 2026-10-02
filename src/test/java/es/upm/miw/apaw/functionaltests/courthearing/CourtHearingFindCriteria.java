package es.upm.miw.apaw.functionaltests.courthearing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CourtHearingFindCriteria {
    private LocalDate date;
    private Boolean scheduled;
    private String courtCity;
    private String userMobile;
    private Boolean all;
}