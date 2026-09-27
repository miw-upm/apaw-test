package es.upm.miw.apaw.functionaltests.leases;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeaseFindCriteria {
    private LeaseType leaseType;
    private Boolean inForce;
    private AmendmentType amendmentType;
    private String userMobile;
}
