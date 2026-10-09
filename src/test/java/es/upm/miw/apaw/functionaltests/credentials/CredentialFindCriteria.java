package es.upm.miw.apaw.functionaltests.credentials;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CredentialFindCriteria {
    private CredentialType credentialType;
    private Boolean expired;
    private VerificationStatus verificationStatus;
    private String userEmail;
}