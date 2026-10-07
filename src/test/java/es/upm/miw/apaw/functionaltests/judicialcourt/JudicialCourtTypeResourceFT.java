package es.upm.miw.apaw.functionaltests.judicialcourt;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = JudicialCourtTypeResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class JudicialCourtTypeResourceFT {

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = JudicialCourtTypeClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private JudicialCourtTypeClient client;

    @Test
    void testCreate() {
        JudicialCourtType type = this.client.create(JudicialCourtType.builder()
                .name("Juzgado test " + UUID.randomUUID())
                .description("Test judicial court type")
                .code("JCT" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .jurisdiction("Penal")
                .build());

        assertThat(type.getId()).isNotNull();
        assertThat(type.getName()).isNotEmpty();
        assertThat(type.getCode()).isNotEmpty();
        assertThat(type.getActive()).isTrue();
    }

    @Test
    void testFindAll() {
        JudicialCourtType created = this.client.create(JudicialCourtType.builder()
                .name("Juzgado findall " + UUID.randomUUID())
                .description("For findall test")
                .code("FA" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .jurisdiction("Civil")
                .build());

        assertThat(this.client.findAll())
                .extracting(JudicialCourtType::getId)
                .contains(created.getId());
    }

    @Test
    void testRead() {
        JudicialCourtType created = this.client.create(JudicialCourtType.builder()
                .name("Juzgado read " + UUID.randomUUID())
                .description("For read test")
                .code("RD" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .jurisdiction("Mercantil")
                .build());

        JudicialCourtType read = this.client.read(created.getId());

        assertThat(read).usingRecursiveComparison().isEqualTo(created);
    }

    @Test
    void testUpdate() {
        JudicialCourtType created = this.client.create(JudicialCourtType.builder()
                .name("Juzgado update " + UUID.randomUUID())
                .description("For update test")
                .code("UP" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .jurisdiction("Social")
                .build());

        JudicialCourtType updated = this.client.update(created.getId(),
                JudicialCourtType.builder()
                        .name("Updated " + UUID.randomUUID())
                        .description("Updated description")
                        .code("UPD" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                        .jurisdiction("Laboral")
                        .active(false)
                        .build());

        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getName()).doesNotContain("Juzgado update");
        assertThat(updated.getActive()).isFalse();
    }

    @Test
    void testPatch() {
        JudicialCourtType created = this.client.create(JudicialCourtType.builder()
                .name("Juzgado patch " + UUID.randomUUID())
                .description("For patch test")
                .code("PT" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .jurisdiction("Contencioso")
                .active(true)
                .build());

        JudicialCourtType patched = this.client.patch(created.getId(),
                new JudicialCourtTypeUpdate("Patched " + UUID.randomUUID(), null, null, null, false));

        assertThat(patched.getId()).isEqualTo(created.getId());
        assertThat(patched.getName()).contains("Patched");
        assertThat(patched.getDescription()).isEqualTo(created.getDescription());
        assertThat(patched.getActive()).isFalse();
    }

    @Test
    void testDelete() {
        JudicialCourtType created = this.client.create(JudicialCourtType.builder()
                .name("Juzgado delete " + UUID.randomUUID())
                .description("For delete test")
                .code("DL" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .jurisdiction("Paz")
                .build());

        this.client.delete(created.getId());

        assertThatThrownBy(() -> this.client.read(created.getId()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateDuplicateName() {
        String uniqueName = "Juzgado unique " + UUID.randomUUID();
        this.client.create(JudicialCourtType.builder()
                .name(uniqueName)
                .description("First")
                .code("C1" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .jurisdiction("Penal")
                .build());

        assertThatThrownBy(() -> this.client.create(JudicialCourtType.builder()
                .name(uniqueName)
                .description("Second")
                .code("C2" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .jurisdiction("Penal")
                .build()))
                .isInstanceOf(FeignException.Conflict.class);
    }
}
