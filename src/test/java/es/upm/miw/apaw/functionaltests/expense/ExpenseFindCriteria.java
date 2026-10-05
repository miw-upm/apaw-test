package es.upm.miw.apaw.functionaltests.expense;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseFindCriteria {
    private String category;
    private Boolean unpaid;
    private String supplierTaxId;
    private String userMobile;
}