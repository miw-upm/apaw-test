package es.upm.miw.apaw.functionaltests.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserFindCriteria {
    private Boolean active;
    private String mobile;
    private Boolean billable;
}
