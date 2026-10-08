package es.upm.miw.apaw.functionaltests.immigrationissues;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ImmigrationIssueFindCriteria {
    private String clientNationality;
    private Boolean overdue;
    private String lawName;
    private String familyName;
}
