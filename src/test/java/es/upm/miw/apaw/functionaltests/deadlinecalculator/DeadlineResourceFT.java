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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = DeadlineResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class DeadlineResourceFT {
    private static final String DEADLINE_PREFIX = "dddddddd-1111-2222-3333-44445555";
    private static final UUID DEADLINE_ID_0 = UUID.fromString(DEADLINE_PREFIX + "1000");
    private static final UUID DEADLINE_ID_1 = UUID.fromString(DEADLINE_PREFIX + "1001");
    private static final UUID DEADLINE_ID_2 = UUID.fromString(DEADLINE_PREFIX + "1002");
    private static final UUID DEADLINE_ID_3 = UUID.fromString(DEADLINE_PREFIX + "1003");
    private static final UUID DEADLINE_ID_4 = UUID.fromString(DEADLINE_PREFIX + "1004");
    private static final UUID DEADLINE_ID_5 = UUID.fromString(DEADLINE_PREFIX + "1005");
    private static final UUID DEADLINE_ID_6 = UUID.fromString(DEADLINE_PREFIX + "1006");
    private static final UUID DEADLINE_ID_7 = UUID.fromString(DEADLINE_PREFIX + "1007");
    private static final UUID DEADLINE_ID_8 = UUID.fromString(DEADLINE_PREFIX + "1008");
    private static final UUID DEADLINE_ID_9 = UUID.fromString(DEADLINE_PREFIX + "1009");

    private static final UUID LAWYER_FT = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0009");
    private static final UUID UNKNOWN_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff9999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = {DeadlineClient.class, NonWorkingDayClient.class})
    static class ClientConfiguration {
    }

    @Autowired
    private DeadlineClient client;
    @Autowired
    private NonWorkingDayClient nonWorkingDayClient;

    private CreationDeadline.CreationDeadlineBuilder creation() {
        return CreationDeadline.builder()
                .title("Plazo FT " + UUID.randomUUID())
                .notificationDate(LocalDate.of(2035, 5, 9))
                .days(3)
                .region("Region FT")
                .city("City FT")
                .userId(LAWYER_FT);
    }

    @Test
    void testCreate() {
        CreationDeadline creation = this.creation().courtFileNumber("500/2035").build();

        Deadline created = this.client.create(creation);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getTitle()).isEqualTo(creation.getTitle());
        assertThat(created.getCourtFileNumber()).isEqualTo("500/2035");
        assertThat(created.getNotificationDate()).isEqualTo(creation.getNotificationDate());
        assertThat(created.getDays()).isEqualTo(3);
        assertThat(created.getRegion()).isEqualTo("Region FT");
        assertThat(created.getCity()).isEqualTo("City FT");
        assertThat(created.getStatus()).isEqualTo(DeadlineStatus.PENDING);
        assertThat(created.getDayCountType()).isEqualTo(DayCountType.WORKING);
        assertThat(created.getCreatedAt()).isNotNull();
        assertThat(created.getDueDate()).isEqualTo(LocalDate.of(2035, 5, 14));
        assertThat(created.getNonWorkingDays()).isEmpty();
        assertThat(created.getLawyer().getId()).isEqualTo(LAWYER_FT);
        assertThat(created.getLawyer().getMobile()).isEqualTo("600000109");
        assertThat(this.client.find(DeadlineFindCriteria.builder().userMobile("600000109").build()))
                .extracting(Deadline::getId).contains(created.getId());
    }

    @Test
    void testCreateSkipsAnApplicableHoliday() {
        String city = "City FT " + UUID.randomUUID();
        NonWorkingDay holiday = this.nonWorkingDayClient.create(NonWorkingDay.builder()
                .date(LocalDate.of(2035, 5, 10))
                .description("Festivo FT")
                .scopeLevel(ScopeLevel.LOCAL)
                .region("Region FT")
                .city(city)
                .build());
        Deadline created = this.client.create(this.creation().city(city).build());
        // 9-may-2035 es miércoles: el jueves 10 es festivo, cuentan vie 11, lun 14 y mar 15.
        assertThat(created.getDueDate()).isEqualTo(LocalDate.of(2035, 5, 15));
        assertThat(created.getNonWorkingDays()).extracting(NonWorkingDay::getId)
                .containsExactly(holiday.getId());
    }

    @Test
    void testCreateWithCalendarDays() {
        CreationDeadline creation = this.creation()
                .days(10).dayCountType(DayCountType.CALENDAR).build();

        Deadline created = this.client.create(creation);

        assertThat(created.getDueDate()).isEqualTo(LocalDate.of(2035, 5, 19));
        assertThat(created.getNonWorkingDays()).isEmpty();
    }

    @Test
    void testCreateDuplicateTitle() {
        CreationDeadline creation = this.creation().title("Alegaciones previas - Madrid").build();

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateUnknownUser() {
        CreationDeadline creation = this.creation().userId(UNKNOWN_ID).build();

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidTitle(String title) {
        CreationDeadline creation = this.creation().title(title).build();

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutNotificationDate() {
        CreationDeadline creation = this.creation().notificationDate(null).build();

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1})
    void testCreateWithNonPositiveDays(int days) {
        CreationDeadline creation = this.creation().days(days).build();

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutDays() {
        CreationDeadline creation = this.creation().days(null).build();

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateWithInvalidRegion(String region) {
        CreationDeadline creation = this.creation().region(region).build();

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateWithInvalidCity(String city) {
        CreationDeadline creation = this.creation().city(city).build();

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutUser() {
        CreationDeadline creation = this.creation().userId(null).build();

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testFindAll() {
        assertThat(this.client.find(new DeadlineFindCriteria())).extracting(Deadline::getId)
                .contains(DEADLINE_ID_0, DEADLINE_ID_1, DEADLINE_ID_2, DEADLINE_ID_3, DEADLINE_ID_4,
                        DEADLINE_ID_5, DEADLINE_ID_6, DEADLINE_ID_7, DEADLINE_ID_8, DEADLINE_ID_9);
    }

    @Test
    void testFindByRegionReturnsSummary() {
        List<Deadline> deadlines = this.client.find(DeadlineFindCriteria.builder().region("Cataluña").build());
        assertThat(deadlines).extracting(Deadline::getId)
                .contains(DEADLINE_ID_5, DEADLINE_ID_6, DEADLINE_ID_7)
                .doesNotContain(DEADLINE_ID_0);
        assertThat(deadlines).allSatisfy(deadline -> {
                    assertThat(deadline.getNonWorkingDays()).isNull();
                    assertThat(deadline.getLawyer().getMobile()).isEqualTo("600000101");
                    assertThat(deadline.getLawyer().getFirstName()).isEqualTo("cliente1");
                });
    }

    @Test
    void testFindByOverdue() {
        assertThat(this.client.find(DeadlineFindCriteria.builder().overdue(true).build()))
                .extracting(Deadline::getId)
                .contains(DEADLINE_ID_0, DEADLINE_ID_1, DEADLINE_ID_2, DEADLINE_ID_3,
                        DEADLINE_ID_5, DEADLINE_ID_6, DEADLINE_ID_8, DEADLINE_ID_9)
                .doesNotContain(DEADLINE_ID_4, DEADLINE_ID_7);
        assertThat(this.client.find(DeadlineFindCriteria.builder().overdue(false).build()))
                .extracting(Deadline::getId)
                .contains(DEADLINE_ID_4, DEADLINE_ID_7)
                .doesNotContain(DEADLINE_ID_0, DEADLINE_ID_5);
    }

    @Test
    void testFindByScopeLevel() {
        assertThat(this.client.find(DeadlineFindCriteria.builder().scopeLevel(ScopeLevel.LOCAL).build()))
                .extracting(Deadline::getId)
                .contains(DEADLINE_ID_0, DEADLINE_ID_1)
                .doesNotContain(DEADLINE_ID_5);
        assertThat(this.client.find(DeadlineFindCriteria.builder().scopeLevel(ScopeLevel.REGIONAL).build()))
                .extracting(Deadline::getId)
                .contains(DEADLINE_ID_5)
                .doesNotContain(DEADLINE_ID_0, DEADLINE_ID_1);
    }

    @Test
    void testFindCombinedCriteria() {
        assertThat(this.client.find(DeadlineFindCriteria.builder()
                .region("Madrid").overdue(false).userMobile("600000100").build()))
                .extracting(Deadline::getId).containsExactly(DEADLINE_ID_4);
        assertThat(this.client.find(DeadlineFindCriteria.builder().userMobile("699999998").build()))
                .isEmpty();
    }

    @Test
    void testFindWorkloadReport() {
        List<DeadlineWorkloadReport> reports = this.client.findWorkloadReport();
        assertThat(reports)
                .filteredOn(report -> "600000102".equals(report.getLawyer().getMobile()))
                .singleElement()
                .satisfies(report -> {
                    assertThat(report.getExpiredDeadlineCount()).isEqualTo(2);
                    assertThat(report.getTotalDeadlineCount()).isEqualTo(2);
                    assertThat(report.getHolidayAffectedDeadlineCount()).isZero();
                    assertThat(report.getLawyer().getFirstName()).isEqualTo("cliente2");
                });
        assertThat(reports).first()
                .satisfies(report -> assertThat(report.getLawyer().getMobile()).isEqualTo("600000100"));
    }
}
