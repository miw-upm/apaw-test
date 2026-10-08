package es.upm.miw.apaw.functionaltests.probate;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = HeirResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class HeirResourceFT {
    private static final UUID HEIR_ID = UUID.fromString("c0c0c0c0-d0d0-e0e0-f0f0-a0a0a0a00000");
    private static final UUID UNKNOWN_ID = UUID.fromString("c0c0c0c0-d0d0-e0e0-f0f0-a0a0a0a09999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = HeirClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private HeirClient client;

    @Test
    void testGet() {
        Heir heir = this.client.get(HEIR_ID);
        assertThat(heir.getFullName()).isEqualTo("John Smith");
        assertThat(heir.getNationalId()).isEqualTo("12345678A");
    }

    @Test
    void testGetNotFound() {
        assertThatThrownBy(() -> this.client.get(UNKNOWN_ID))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateUpdatePatchDelete() {
        Heir created = this.client.create(Heir.builder()
                .fullName("Feign heir")
                .nationalId("NID-" + UUID.randomUUID())
                .birthDate(LocalDate.of(1990, 1, 1))
                .sharePercentage(new BigDecimal("25.00"))
                .build());
        assertThat(created.getId()).isNotNull();
        assertThat(created.getHeirStatus()).isEqualTo(HeirStatus.PENDING);

        Heir updated = this.client.update(created.getId(), Heir.builder()
                .fullName("Feign heir updated")
                .nationalId(created.getNationalId())
                .birthDate(created.getBirthDate())
                .sharePercentage(new BigDecimal("75.00"))
                .heirStatus(HeirStatus.ACCEPTED)
                .build());
        assertThat(updated.getFullName()).isEqualTo("Feign heir updated");
        assertThat(updated.getSharePercentage()).isEqualByComparingTo("75.00");

        Heir patched = this.client.patch(created.getId(),
                new HeirUpdate(null, null, null, null, HeirStatus.NOTIFIED, null));
        assertThat(patched.getHeirStatus()).isEqualTo(HeirStatus.NOTIFIED);
        assertThat(patched.getFullName()).isEqualTo("Feign heir updated");

        this.client.delete(created.getId());
        assertThatThrownBy(() -> this.client.get(created.getId()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateDuplicateNationalId() {
        assertThatThrownBy(() -> this.client.create(Heir.builder()
                .fullName("Duplicate")
                .nationalId("NID-" + UUID.randomUUID())
                .birthDate(LocalDate.of(1991, 1, 1))
                .sharePercentage(new BigDecimal("10.00"))
                .build()))
                .isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testDeleteReferenced() {
        assertThatThrownBy(() -> this.client.delete(HEIR_ID))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testListContainsSeeded() {
        List<Heir> heirs = this.client.list();
        assertThat(heirs).extracting(Heir::getId).contains(HEIR_ID);
    }
}
