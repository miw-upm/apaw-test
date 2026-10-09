package es.upm.miw.apaw.functionaltests.powerofattorney.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PowerOfAttorneyParty {
    private UUID id;
    private Integer age;
    private Boolean fullMentalCapacity;
    private String companyName;
    private Boolean representationCompany;
    private UserSnapshot userSnapshot;
}
