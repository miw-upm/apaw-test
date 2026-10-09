package es.upm.miw.apaw.functionaltests.credentials;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialVerificationReport {
    private String credentialNumber;
    private long totalVerificationCount;
    private long verifiedVerificationCount;
}