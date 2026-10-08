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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
        classes = VerificationResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class VerificationResourceFT {

    private static final UUID ID_0 =
            UUID.fromString("dddddddd-eeee-ffff-aaaa-bbbbcccc0000");

    private static final UUID ID_1 =
            UUID.fromString("dddddddd-eeee-ffff-aaaa-bbbbcccc0001");

    private static final UUID ID_2 =
            UUID.fromString("dddddddd-eeee-ffff-aaaa-bbbbcccc0002");

    private static final UUID ID_3 =
            UUID.fromString("dddddddd-eeee-ffff-aaaa-bbbbcccc0003");

    private static final UUID ID_4 =
            UUID.fromString("dddddddd-eeee-ffff-aaaa-bbbbcccc0004");

    private static final UUID UNKNOWN_ID =
            UUID.fromString("dddddddd-eeee-ffff-aaaa-bbbbcccc9999");

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EnableFeignClients(clients = VerificationClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private VerificationClient client;

    @Test
    void testRead() {
        Verification actual = this.client.read(ID_0);

        assertThat(actual.getId()).isEqualTo(ID_0);
        assertThat(actual.getCreatedAt())
                .isEqualTo(LocalDateTime.of(2025, 1, 10, 9, 0));
        assertThat(actual.getMethod()).isEqualTo("DOCUMENT_REVIEW");
        assertThat(actual.getName()).isEqualTo("Initial documentation review");
        assertThat(actual.getNotes())
                .isEqualTo("Documentation is pending verification");
        assertThat(actual.getVerifiedAt()).isNull();
        assertThat(actual.getScore()).isNull();
        assertThat(actual.getVerificationStatus())
                .isEqualTo(VerificationStatus.PENDING);
    }

    @Test
    void testReadNotFound() {
        assertThatThrownBy(() -> this.client.read(UNKNOWN_ID))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testFindAll() {
        assertThat(this.client.findAll())
                .extracting(Verification::getId)
                .contains(ID_0, ID_1, ID_2, ID_3, ID_4);
    }

    @Test
    void testCreateAndDelete() {
        Verification creation = Verification.builder()
                .method("FT_METHOD")
                .name("FT verification " + UUID.randomUUID())
                .notes("Created through functional test")
                .score(new BigDecimal("75.00"))
                .verifiedAt(LocalDateTime.of(2025, 8, 2, 10, 0))
                .build();

        Verification created = this.client.create(creation);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(created.getMethod()).isEqualTo(creation.getMethod());
        assertThat(created.getName()).isEqualTo(creation.getName());
        assertThat(created.getNotes()).isEqualTo(creation.getNotes());
        assertThat(created.getScore()).isEqualTo(creation.getScore());
        assertThat(created.getVerifiedAt()).isEqualTo(creation.getVerifiedAt());
        assertThat(created.getVerificationStatus())
                .isEqualTo(VerificationStatus.PENDING);

        this.client.delete(created.getId());

        assertThatThrownBy(() -> this.client.read(created.getId()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testUpdate() {
        Verification created = this.createVerification();
        Verification storedBeforeUpdate = this.client.read(created.getId());

        Verification replacement = Verification.builder()
                .method("UPDATED_METHOD")
                .name("Updated verification")
                .notes("Updated notes")
                .score(new BigDecimal("90.00"))
                .verifiedAt(LocalDateTime.of(2025, 8, 3, 10, 0))
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();

        Verification updated = this.client.update(created.getId(), replacement);

        assertThat(updated.getId()).isEqualTo(storedBeforeUpdate.getId());
        assertThat(updated.getCreatedAt()).isEqualTo(storedBeforeUpdate.getCreatedAt());
        assertThat(updated.getMethod()).isEqualTo(replacement.getMethod());
        assertThat(updated.getName()).isEqualTo(replacement.getName());
        assertThat(updated.getNotes()).isEqualTo(replacement.getNotes());
        assertThat(updated.getScore()).isEqualTo(replacement.getScore());
        assertThat(updated.getVerifiedAt()).isEqualTo(replacement.getVerifiedAt());
        assertThat(updated.getVerificationStatus())
                .isEqualTo(replacement.getVerificationStatus());

        this.client.delete(created.getId());
    }

    @Test
    void testUpdateNotFound() {
        Verification replacement = Verification.builder()
                .method("UPDATED_METHOD")
                .build();

        assertThatThrownBy(() -> this.client.update(UNKNOWN_ID, replacement))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testPatch() {
        Verification created = this.createVerification();
        Verification storedBeforePatch = this.client.read(created.getId());

        VerificationPatch patch = VerificationPatch.builder()
                .method("PATCH_METHOD")
                .notes("Patched notes")
                .verificationStatus(VerificationStatus.VERIFIED)
                .build();

        Verification patched = this.client.patch(created.getId(), patch);

        assertThat(patched.getId()).isEqualTo(storedBeforePatch.getId());
        assertThat(patched.getCreatedAt()).isEqualTo(storedBeforePatch.getCreatedAt());
        assertThat(patched.getMethod()).isEqualTo("PATCH_METHOD");
        assertThat(patched.getNotes()).isEqualTo("Patched notes");
        assertThat(patched.getVerificationStatus())
                .isEqualTo(VerificationStatus.VERIFIED);
        assertThat(patched.getName()).isEqualTo(storedBeforePatch.getName());
        assertThat(patched.getScore()).isEqualTo(storedBeforePatch.getScore());
        assertThat(patched.getVerifiedAt()).isEqualTo(storedBeforePatch.getVerifiedAt());

        this.client.delete(created.getId());
    }

    @Test
    void testPatchNotFound() {
        VerificationPatch patch = VerificationPatch.builder()
                .method("PATCH_METHOD")
                .build();

        assertThatThrownBy(() -> this.client.patch(UNKNOWN_ID, patch))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testPatchBlankMethod() {
        Verification created = this.createVerification();

        VerificationPatch patch = VerificationPatch.builder()
                .method(" ")
                .build();

        assertThatThrownBy(() -> this.client.patch(created.getId(), patch))
                .isInstanceOf(FeignException.BadRequest.class);

        this.client.delete(created.getId());
    }

    private Verification createVerification() {
        return this.client.create(
                Verification.builder()
                        .method("FT_METHOD")
                        .name("FT verification " + UUID.randomUUID())
                        .notes("Original notes")
                        .score(new BigDecimal("75.00"))
                        .verifiedAt(LocalDateTime.of(2025, 8, 2, 10, 0))
                        .build());
    }
}