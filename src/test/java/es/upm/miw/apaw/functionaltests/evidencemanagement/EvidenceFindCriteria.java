package es.upm.miw.apaw.functionaltests.evidencemanagement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceFindCriteria {
    private Boolean confidential;
    private Boolean longCustody;
    private String action;
    private String custodianFirstName;
}