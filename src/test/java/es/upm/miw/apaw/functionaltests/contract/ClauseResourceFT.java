package es.upm.miw.apaw.functionaltests.contract;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@SpringBootTest(
        classes = ClauseResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
public class ClauseResourceFT {
    private static final UUID CLAUSE_ID_0 = UUID.fromString("cccccccc-dddd-eeee-ffff-000000000000");
    private static final UUID CLAUSE_ID_1 = UUID.fromString("cccccccc-dddd-eeee-ffff-000000000001");
    private static final UUID CLAUSE_ID_7 = UUID.fromString("cccccccc-dddd-eeee-ffff-000000000007");
    private static final UUID CLAUSE_ID_A = UUID.fromString("cccccccc-dddd-eeee-ffff-00000000000a");
    private static final UUID UNKNOWN_ID = UUID.fromString("cccccccc-dddd-eeee-ffff-999999999999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = ClauseClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private ClauseClient client;

    @Test
    void testRead() {
        Clause clause = this.client.read(CLAUSE_ID_0);
        assertThat(clause.getId()).isEqualTo(CLAUSE_ID_0);
        assertThat(clause.getTitle()).isEqualTo("Confidencialidad");
        assertThat(clause.getType()).isEqualTo(ClauseType.CONFIDENTIALITY);
        assertThat(clause.getContent())
                .isEqualTo("Las partes se comprometen a mantener la confidencialidad de la información compartida.");
        assertThat(clause.getEffectiveFrom()).isEqualTo(LocalDate.of(2025, 1, 1));
        assertThat(clause.getEffectiveUntil()).isEqualTo(LocalDate.of(2027, 12, 31));
        assertThat(clause.getNotes()).isEqualTo("Protección de información sensible.");
        assertThat(clause.getVersion()).isEqualTo(1);
    }

    @Test
    void testReadNotFound() {
        assertThatThrownBy(() -> this.client.read(UNKNOWN_ID))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testFindAll() {
        List<Clause> clauses = this.client.findAll();

        assertThat(clauses)
                .extracting(Clause::getId)
                .contains(
                        CLAUSE_ID_0,
                        CLAUSE_ID_1,
                        CLAUSE_ID_A
                );
    }

    @Test
    void testCreateUpdateAndDelete() {
        Clause clause = Clause.builder()
                .title("Cláusula Feign")
                .type(ClauseType.OTHER)
                .content("Contenido de la cláusula Feign.")
                .effectiveFrom(LocalDate.of(2026, 10, 1))
                .version(1)
                .build();

        Clause created = this.client.create(clause);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getTitle()).isEqualTo("Cláusula Feign");

        created.setTitle("Cláusula Feign actualizada");
        created.setContent("Contenido actualizado de la cláusula Feign.");
        created.setVersion(2);

        Clause updated = this.client.update(created.getId(), created);

        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getTitle()).isEqualTo("Cláusula Feign actualizada");
        assertThat(updated.getContent())
                .isEqualTo("Contenido actualizado de la cláusula Feign.");
        assertThat(updated.getVersion()).isEqualTo(2);

        assertThat(this.client.read(created.getId())).isEqualTo(updated);

        this.client.delete(created.getId());

        assertThatThrownBy(() -> this.client.read(created.getId()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateDefaultType() {
        Clause clause = Clause.builder()
                .title("Cláusula de auditoría")
                .content("Las partes podrán realizar auditorías para verificar el cumplimiento del contrato.")
                .effectiveFrom(LocalDate.of(2026, 10, 1))
                .version(3)
                .build();

        Clause created = this.client.create(clause);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getType()).isEqualTo(ClauseType.OTHER);

        this.client.delete(created.getId());
    }

    @Test
    void testUpdateNotFound() {
        Clause clause = Clause.builder()
                .title("Cláusula inexistente")
                .content("Contenido de prueba.")
                .effectiveFrom(LocalDate.of(2026, 10, 1))
                .version(1)
                .build();

        assertThatThrownBy(() -> this.client.update(UNKNOWN_ID, clause))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testDeleteReferencedClause() {
        assertThatThrownBy(() -> this.client.delete(CLAUSE_ID_7))
                .isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testPatch() {
        Clause clause = Clause.builder()
                .title("Cláusula Patch")
                .type(ClauseType.OTHER)
                .content("Contenido original.")
                .effectiveFrom(LocalDate.of(2026, 10, 1))
                .version(1)
                .build();

        Clause created = this.client.create(clause);

        ClauseUpdate patch = new ClauseUpdate(
                ClauseType.CONFIDENTIALITY,
                "Notas actualizadas.",
                LocalDate.of(2027, 10, 1)
        );

        Clause patched = this.client.patch(created.getId(), patch);

        assertThat(patched.getId()).isEqualTo(created.getId());
        assertThat(patched.getTitle()).isEqualTo("Cláusula Patch");
        assertThat(patched.getType()).isEqualTo(ClauseType.CONFIDENTIALITY);
        assertThat(patched.getContent()).isEqualTo("Contenido original.");
        assertThat(patched.getEffectiveFrom()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(patched.getEffectiveUntil()).isEqualTo(LocalDate.of(2027, 10, 1));
        assertThat(patched.getNotes()).isEqualTo("Notas actualizadas.");
        assertThat(patched.getVersion()).isEqualTo(1);

        this.client.delete(created.getId());
    }

    @Test
    void testPatchNotFound() {
        ClauseUpdate patch = new ClauseUpdate(
                ClauseType.CONFIDENTIALITY,
                "Notas de prueba.",
                LocalDate.of(2027, 10, 1)
        );

        assertThatThrownBy(() -> this.client.patch(UNKNOWN_ID, patch))
                .isInstanceOf(FeignException.NotFound.class);
    }
}
