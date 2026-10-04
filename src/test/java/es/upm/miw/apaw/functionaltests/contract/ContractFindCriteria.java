package es.upm.miw.apaw.functionaltests.contract;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContractFindCriteria {
    private String title;
    private Boolean active;
    private ClauseType clauseType;
    private String userCity;
}