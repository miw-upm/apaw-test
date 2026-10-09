package es.upm.miw.apaw.functionaltests.credentials;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VerificationPatch {
    private LocalDateTime verifiedAt;
    private String method;
    private String name;
    private String notes;
    private BigDecimal score;
    private VerificationStatus verificationStatus;
}