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
public class CreationCredential {
    private String number;
    private String registryCode;
    private String authority;
    private LocalDate issueDate;
    private LocalDate expirationDate;
    private CredentialType credentialType;
    private List<UUID> verificationIds;
    private UUID userId;
}