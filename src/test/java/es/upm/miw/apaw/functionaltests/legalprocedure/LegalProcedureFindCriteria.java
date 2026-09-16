package es.upm.miw.apaw.functionaltests.legalprocedure;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegalProcedureFindCriteria {
    private Boolean vatIncluded;
    private Boolean opened;
    private TaskStatus taskStatus;
    private String userMobile;
}
