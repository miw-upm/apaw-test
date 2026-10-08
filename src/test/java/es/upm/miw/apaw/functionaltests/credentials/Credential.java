package es.upm.miw.apaw.functionaltests.credentials;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Credential {
    private UUID id;
    private String number;
    private String registryCode;
    private String authority;
    private LocalDate issueDate;
    private LocalDate expirationDate;
    private Integer renewalCount;
    private Boolean renewable;
    private CredentialType credentialType;
    private List<Verification> verifications;
    private UserSnapshot user;
}