package es.upm.miw.apaw.functionaltests.secondlawchance;

import lombok.*;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExonerationCaseFindCriteria {
    private String lawyer;
    private Boolean opened;
    private CreditorType creditorType;
    private String userMobile;
}
