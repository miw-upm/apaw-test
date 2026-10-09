package es.upm.miw.apaw.functionaltests.credentials;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
        classes = CredentialResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class CredentialResourceFT {

    private static final UUID USER_0 =
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");

    private static final UUID USER_1 =
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0001");

    private static final String USER_0_EMAIL = "cliente0@example.com";
    private static final String USER_1_EMAIL = "cliente1@example.com";

    private static final UUID UNKNOWN_ID =
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff9999");

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EnableFeignClients(clients = {
            CredentialClient.class,
            VerificationClient.class
    })
    static class ClientConfiguration {
    }

    @Autowired
    private CredentialClient credentialClient;

    @Autowired
    private VerificationClient verificationClient;

    @Test
    void testCreate() {
        Verification verification = this.createVerification(
                "CREATE_" + UUID.randomUUID());

        CreationCredential creation = this.creation(
                USER_0,
                List.of(verification.getId()));

        Credential actual = this.credentialClient.create(creation);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getNumber()).isEqualTo(creation.getNumber());
        assertThat(actual.getRegistryCode()).isEqualTo(creation.getRegistryCode());
        assertThat(actual.getAuthority()).isEqualTo(creation.getAuthority());
        assertThat(actual.getIssueDate()).isEqualTo(creation.getIssueDate());
        assertThat(actual.getExpirationDate()).isEqualTo(creation.getExpirationDate());
        assertThat(actual.getCredentialType()).isEqualTo(creation.getCredentialType());

        assertThat(actual.getRenewalCount()).isZero();
        assertThat(actual.getRenewable()).isTrue();

        assertThat(actual.getVerifications())
                .extracting(Verification::getId)
                .containsExactly(verification.getId());

        assertThat(actual.getUser().getId()).isEqualTo(USER_0);
        assertThat(actual.getUser().getEmail()).isEqualTo(USER_0_EMAIL);
    }

    @Test
    void testCreateWithoutVerifications() {
        CreationCredential creation = this.creation(
                USER_0,
                List.of());

        Credential actual = this.credentialClient.create(creation);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getRenewalCount()).isZero();
        assertThat(actual.getRenewable()).isTrue();
        assertThat(actual.getVerifications()).isEmpty();
        assertThat(actual.getUser().getId()).isEqualTo(USER_0);
    }

    @Test
    void testCreateDuplicateNumber() {
        String number = "DUPLICATE-" + UUID.randomUUID();

        CreationCredential first = this.creation(
                USER_0,
                List.of());
        first.setNumber(number);

        this.credentialClient.create(first);

        CreationCredential duplicate = this.creation(
                USER_0,
                List.of());
        duplicate.setNumber(number);

        assertThatThrownBy(() -> this.credentialClient.create(duplicate))
                .isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateDuplicateRegistryCode() {
        String registryCode = "REG-" + UUID.randomUUID();

        CreationCredential first = this.creation(
                USER_0,
                List.of());
        first.setRegistryCode(registryCode);

        this.credentialClient.create(first);

        CreationCredential duplicate = this.creation(
                USER_0,
                List.of());
        duplicate.setRegistryCode(registryCode);

        assertThatThrownBy(() -> this.credentialClient.create(duplicate))
                .isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateUnknownVerification() {
        UUID verificationId = UUID.randomUUID();

        CreationCredential creation = this.creation(
                USER_0,
                List.of(verificationId));

        assertThatThrownBy(() -> this.credentialClient.create(creation))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateUnknownUser() {
        CreationCredential creation = this.creation(
                UNKNOWN_ID,
                List.of());

        assertThatThrownBy(() -> this.credentialClient.create(creation))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidNumber(String number) {
        CreationCredential creation = this.creation(
                USER_0,
                List.of());
        creation.setNumber(number);

        assertThatThrownBy(() -> this.credentialClient.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidAuthority(String authority) {
        CreationCredential creation = this.creation(
                USER_0,
                List.of());
        creation.setAuthority(authority);

        assertThatThrownBy(() -> this.credentialClient.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutIssueDate() {
        CreationCredential creation = this.creation(
                USER_0,
                List.of());
        creation.setIssueDate(null);

        assertThatThrownBy(() -> this.credentialClient.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutCredentialType() {
        CreationCredential creation = this.creation(
                USER_0,
                List.of());
        creation.setCredentialType(null);

        assertThatThrownBy(() -> this.credentialClient.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutUser() {
        CreationCredential creation = this.creation(
                USER_0,
                List.of());
        creation.setUserId(null);

        assertThatThrownBy(() -> this.credentialClient.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutVerificationIds() {
        CreationCredential creation = this.creation(
                USER_0,
                List.of());
        creation.setVerificationIds(null);

        assertThatThrownBy(() -> this.credentialClient.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateNullVerificationId() {
        CreationCredential creation = this.creation(
                USER_0,
                Arrays.asList((UUID) null));

        assertThatThrownBy(() -> this.credentialClient.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testFindAll() {
        CreationCredential creation = this.creation(
                USER_0,
                List.of());

        Credential created = this.credentialClient.create(creation);

        assertThat(this.credentialClient.find(new CredentialFindCriteria()))
                .extracting(Credential::getId)
                .contains(created.getId());
    }

    @Test
    void testFindByCredentialType() {
        Credential license = this.credentialClient.create(
                this.creation(
                        USER_0,
                        List.of(),
                        CredentialType.LICENSE));

        Credential registration = this.credentialClient.create(
                this.creation(
                        USER_0,
                        List.of(),
                        CredentialType.REGISTRATION));

        assertThat(this.credentialClient.find(
                CredentialFindCriteria.builder()
                        .credentialType(CredentialType.LICENSE)
                        .build()))
                .extracting(Credential::getId)
                .contains(license.getId())
                .doesNotContain(registration.getId());
    }

    @Test
    void testFindByExpired() {
        Credential expired = this.credentialClient.create(
                this.creation(
                        USER_0,
                        List.of(),
                        CredentialType.CERTIFICATION,
                        LocalDate.of(2020, 1, 1)));

        Credential notExpired = this.credentialClient.create(
                this.creation(
                        USER_0,
                        List.of(),
                        CredentialType.CERTIFICATION,
                        LocalDate.of(2099, 1, 1)));

        assertThat(this.credentialClient.find(
                CredentialFindCriteria.builder()
                        .expired(true)
                        .build()))
                .extracting(Credential::getId)
                .contains(expired.getId())
                .doesNotContain(notExpired.getId());

        assertThat(this.credentialClient.find(
                CredentialFindCriteria.builder()
                        .expired(false)
                        .build()))
                .extracting(Credential::getId)
                .contains(notExpired.getId())
                .doesNotContain(expired.getId());
    }

    @Test
    void testFindByVerificationStatus() {
        Verification pending = this.createVerification(
                "STATUS_PENDING_" + UUID.randomUUID());

        Verification verified = this.createVerification(
                "STATUS_VERIFIED_" + UUID.randomUUID());

        verified = this.verificationClient.patch(
                verified.getId(),
                VerificationPatch.builder()
                        .verificationStatus(VerificationStatus.VERIFIED)
                        .build());

        Credential pendingCredential = this.credentialClient.create(
                this.creation(USER_0, List.of(pending.getId())));

        Credential verifiedCredential = this.credentialClient.create(
                this.creation(USER_0, List.of(verified.getId())));

        assertThat(this.credentialClient.find(
                CredentialFindCriteria.builder()
                        .verificationStatus(VerificationStatus.VERIFIED)
                        .build()))
                .extracting(Credential::getId)
                .contains(verifiedCredential.getId())
                .doesNotContain(pendingCredential.getId());
    }

    @Test
    void testFindByUserEmail() {
        Credential first = this.credentialClient.create(
                this.creation(
                        USER_0,
                        List.of(),
                        CredentialType.REGISTRATION));

        Credential second = this.credentialClient.create(
                this.creation(
                        USER_1,
                        List.of(),
                        CredentialType.REGISTRATION));

        List<Credential> credentials = this.credentialClient.find(
                CredentialFindCriteria.builder()
                        .userEmail(USER_0_EMAIL)
                        .build());

        assertThat(credentials)
                .extracting(Credential::getId)
                .contains(first.getId())
                .doesNotContain(second.getId());

        assertThat(credentials)
                .filteredOn(credential -> credential.getId().equals(first.getId()))
                .singleElement()
                .satisfies(credential -> {
                    assertThat(credential.getNumber())
                            .isEqualTo(first.getNumber());
                    assertThat(credential.getUser().getEmail())
                            .isEqualTo(USER_0_EMAIL);
                    assertThat(credential.getVerifications())
                            .isNull();
                });

        assertThat(this.credentialClient.find(
                CredentialFindCriteria.builder()
                        .userEmail("unknown-" + UUID.randomUUID() + "@example.com")
                        .build()))
                .isEmpty();
    }

    @Test
    void testFindCombinedCriteria() {
        Credential matching = this.credentialClient.create(
                this.creation(
                        USER_0,
                        List.of(),
                        CredentialType.ACCREDITATION,
                        LocalDate.of(2099, 1, 1)));

        Credential wrongEmail = this.credentialClient.create(
                this.creation(
                        USER_1,
                        List.of(),
                        CredentialType.ACCREDITATION,
                        LocalDate.of(2099, 1, 1)));

        Credential wrongType = this.credentialClient.create(
                this.creation(
                        USER_0,
                        List.of(),
                        CredentialType.LICENSE,
                        LocalDate.of(2099, 1, 1)));

        assertThat(this.credentialClient.find(
                CredentialFindCriteria.builder()
                        .credentialType(CredentialType.ACCREDITATION)
                        .expired(false)
                        .userEmail(USER_0_EMAIL)
                        .build()))
                .extracting(Credential::getId)
                .contains(matching.getId())
                .doesNotContain(wrongEmail.getId(), wrongType.getId());
    }

    @Test
    void testFindByExpiredWithoutExpirationDate() {
        Credential withoutExpiration = this.credentialClient.create(
                this.creation(
                        USER_0,
                        List.of(),
                        CredentialType.REGISTRATION,
                        null));

        assertThat(this.credentialClient.find(
                CredentialFindCriteria.builder()
                        .expired(false)
                        .build()))
                .extracting(Credential::getId)
                .contains(withoutExpiration.getId());

        assertThat(this.credentialClient.find(
                CredentialFindCriteria.builder()
                        .expired(true)
                        .build()))
                .extracting(Credential::getId)
                .doesNotContain(withoutExpiration.getId());
    }

    @Test
    void testFindVerificationReport() {
        Verification pending = this.createVerification(
                "REPORT_PENDING_" + UUID.randomUUID());

        Verification verified1 = this.createVerification(
                "REPORT_VERIFIED_1_" + UUID.randomUUID());

        Verification verified2 = this.createVerification(
                "REPORT_VERIFIED_2_" + UUID.randomUUID());

        verified1 = this.verificationClient.patch(
                verified1.getId(),
                VerificationPatch.builder()
                        .verificationStatus(VerificationStatus.VERIFIED)
                        .build());

        verified2 = this.verificationClient.patch(
                verified2.getId(),
                VerificationPatch.builder()
                        .verificationStatus(VerificationStatus.VERIFIED)
                        .build());

        Credential credential = this.credentialClient.create(
                this.creation(
                        USER_0,
                        List.of(
                                pending.getId(),
                                verified1.getId(),
                                verified2.getId())));

        List<CredentialVerificationReport> report =
                this.credentialClient.findVerificationReport();

        assertThat(report)
                .extracting(CredentialVerificationReport::getTotalVerificationCount)
                .isSortedAccordingTo(Comparator.reverseOrder());

        assertThat(report)
                .filteredOn(item ->
                        item.getCredentialNumber()
                                .equals(credential.getNumber()))
                .singleElement()
                .satisfies(item -> {
                    assertThat(item.getTotalVerificationCount()).isEqualTo(3);
                    assertThat(item.getVerifiedVerificationCount()).isEqualTo(2);
                });
    }

    private CreationCredential creation(
            UUID userId,
            List<UUID> verificationIds) {
        return this.creation(
                userId,
                verificationIds,
                CredentialType.CERTIFICATION,
                LocalDate.of(2030, 1, 1));
    }

    private CreationCredential creation(
            UUID userId,
            List<UUID> verificationIds,
            CredentialType credentialType) {
        return this.creation(
                userId,
                verificationIds,
                credentialType,
                LocalDate.of(2030, 1, 1));
    }

    private CreationCredential creation(
            UUID userId,
            List<UUID> verificationIds,
            CredentialType credentialType,
            LocalDate expirationDate) {
        return CreationCredential.builder()
                .number("FT-CREDENTIAL-" + UUID.randomUUID())
                .registryCode("FT-REGISTRY-" + UUID.randomUUID())
                .authority("FT Authority")
                .issueDate(LocalDate.of(2025, 1, 1))
                .expirationDate(expirationDate)
                .credentialType(credentialType)
                .verificationIds(verificationIds)
                .userId(userId)
                .build();
    }

    private Verification createVerification(String method) {
        return this.verificationClient.create(
                Verification.builder()
                        .method(method)
                        .name("FT verification " + UUID.randomUUID())
                        .notes("Created by functional test")
                        .build());
    }
}