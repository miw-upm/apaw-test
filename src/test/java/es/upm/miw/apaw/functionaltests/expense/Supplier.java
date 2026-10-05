package es.upm.miw.apaw.functionaltests.expense;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {
    private UUID id;
    private String taxId;
    private String companyName;
    private String address;
    private String contactEmail;
    private String corporatePhone;
    private Integer paymentTermsDays;
}