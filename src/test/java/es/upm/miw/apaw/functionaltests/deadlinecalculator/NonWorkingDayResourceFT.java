package es.upm.miw.apaw.functionaltests.deadlinecalculator;

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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = NonWorkingDayResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class NonWorkingDayResourceFT {
    private static final UUID NEW_YEAR_ID = UUID.fromString("dddddddd-1111-2222-3333-444455550000");
    private static final UUID REFERENCED_ID = UUID.fromString("dddddddd-1111-2222-3333-444455550010");
    private static final UUID UNKNOWN_ID = UUID.fromString("dddddddd-1111-2222-3333-444455559999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = NonWorkingDayClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private NonWorkingDayClient client;

    private NonWorkingDay.NonWorkingDayBuilder local() {
        return NonWorkingDay.builder()
                .date(LocalDate.of(2031, 3, 3))
                .description("Festivo FT")
                .scopeLevel(ScopeLevel.LOCAL)
                .region("Region FT")
                .city("City FT " + UUID.randomUUID());
    }

    @Test
    void testRead() {
        NonWorkingDay nonWorkingDay = this.client.read(NEW_YEAR_ID);

        assertThat(nonWorkingDay.getDescription()).isEqualTo("Año Nuevo");
        assertThat(nonWorkingDay.getDate()).isEqualTo(LocalDate.of(2026, 1, 1));
        assertThat(nonWorkingDay.getScopeLevel()).isEqualTo(ScopeLevel.NATIONAL);
        assertThat(nonWorkingDay.getRegion()).isNull();
        assertThat(nonWorkingDay.getCity()).isNull();
        assertThat(nonWorkingDay.getRecurring()).isTrue();
    }

    @Test
    void testReadNotFound() {
        assertThatThrownBy(() -> this.client.read(UNKNOWN_ID))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testFindAll() {
        assertThat(this.client.findAll()).extracting(NonWorkingDay::getDescription)
                .contains("Año Nuevo", "San Isidro", "La Mercè");
    }

    @Test
    void testCreateUpdatePatchAndDelete() {
        NonWorkingDay created = this.client.create(this.local().build());
        assertThat(created.getId()).isNotNull();
        assertThat(created.getRecurring()).isFalse();

        created.setDescription("Festivo FT corregido");
        created.setDate(LocalDate.of(2031, 3, 4));
        NonWorkingDay updated = this.client.update(created.getId(), created);
        assertThat(updated).usingRecursiveComparison().isEqualTo(created);
        assertThat(this.client.read(created.getId())).usingRecursiveComparison().isEqualTo(created);

        this.client.updateRecurrences(List.of(new NonWorkingDayRecurringUpdate(created.getId(), true)));
        assertThat(this.client.read(created.getId()).getRecurring()).isTrue();

        this.client.delete(created.getId());
        assertThatThrownBy(() -> this.client.read(created.getId()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateDuplicateConflict() {
        NonWorkingDay duplicated = NonWorkingDay.builder()
                .date(LocalDate.of(2026, 1, 1))
                .description("Año Nuevo FT")
                .scopeLevel(ScopeLevel.NATIONAL)
                .build();

        assertThatThrownBy(() -> this.client.create(duplicated))
                .isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateInconsistentScopeBadRequest() {
        NonWorkingDay inconsistent = this.local()
                .scopeLevel(ScopeLevel.NATIONAL).region("Madrid").city(null).build();

        assertThatThrownBy(() -> this.client.create(inconsistent))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidDescription(String description) {
        NonWorkingDay invalid = this.local().description(description).build();

        assertThatThrownBy(() -> this.client.create(invalid))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testUpdateNotFound() {
        NonWorkingDay nonWorkingDay = this.local().build();

        assertThatThrownBy(() -> this.client.update(UNKNOWN_ID, nonWorkingDay))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testUpdateReferencedDateConflict() {
        NonWorkingDay referenced = this.client.read(REFERENCED_ID);
        referenced.setDate(LocalDate.of(2026, 6, 16));

        assertThatThrownBy(() -> this.client.update(REFERENCED_ID, referenced))
                .isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testUpdateInconsistentScopeBadRequest() {
        NonWorkingDay created = this.client.create(this.local().build());
        created.setScopeLevel(ScopeLevel.REGIONAL);
        created.setRegion(null);

        assertThatThrownBy(() -> this.client.update(created.getId(), created))
                .isInstanceOf(FeignException.BadRequest.class);

        this.client.delete(created.getId());
    }

    @Test
    void testUpdateRecurrencesWithAnUnknownIdNotFound() {
        List<NonWorkingDayRecurringUpdate> updates =
                List.of(new NonWorkingDayRecurringUpdate(UNKNOWN_ID, true));

        assertThatThrownBy(() -> this.client.updateRecurrences(updates))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testUpdateRecurrencesWithAnEmptyListBadRequest() {
        List<NonWorkingDayRecurringUpdate> updates = List.of();

        assertThatThrownBy(() -> this.client.updateRecurrences(updates))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testDeleteUnknownIdIsIdempotent() {
        assertThatCode(() -> this.client.delete(UNKNOWN_ID)).doesNotThrowAnyException();
    }

    @Test
    void testDeleteReferencedConflict() {
        assertThatThrownBy(() -> this.client.delete(REFERENCED_ID))
                .isInstanceOf(FeignException.Conflict.class);
    }
}
