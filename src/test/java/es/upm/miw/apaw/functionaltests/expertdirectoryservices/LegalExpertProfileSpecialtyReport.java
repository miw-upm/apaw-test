package es.upm.miw.apaw.functionaltests.expertdirectoryservices;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegalExpertProfileSpecialtyReport {
    private String specialtyArea;
    private long totalProfiles;
    private long totalSchedules;
    private double averageRateAmount;
    private double averageYearsOfExperience;
    private UserSnapshot mostVeteranExpert;
}
