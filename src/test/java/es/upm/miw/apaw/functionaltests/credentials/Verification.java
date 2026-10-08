package es.upm.miw.apaw.functionaltests.credentials;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Verification {
    private UUID id;
    private LocalDateTime createdAt;
    private LocalDateTime verifiedAt;
    private String method;
    private String name;
    private String notes;
    private BigDecimal score;
    private VerificationStatus verificationStatus;
}