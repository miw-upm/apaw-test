package es.upm.miw.apaw.functionaltests.expertdirectoryservices;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegalExpertProfile {
    private UUID id;
    private String taxIdCode;
    private String professionalLicense;
    private String specialtyArea;
    private Integer yearsOfExperience;
    private Boolean requiresPrepayment;
    private LocalDate partnershipDate;
    private UserSnapshot userSnapshot;
}
