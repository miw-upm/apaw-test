package es.upm.miw.apaw.functionaltests.stucktaskdetector;

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
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = StuckTaskRuleResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class StuckTaskRuleResourceFT {
    private static final UUID USER_ID_0 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");
    private static final UUID USER_ID_1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0001");
    private static final UUID UNKNOWN_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff9999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = StuckTaskRuleClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private StuckTaskRuleClient client;

    private StuckTaskRuleCreation creation() {
        return StuckTaskRuleCreation.builder().name("FT rule " + UUID.randomUUID())
                .procedureKeyword("labour").thresholdDays(20)
                .penaltyAmount(new BigDecimal("75.50")).userId(USER_ID_0).build();
    }

    @Test
    void testCreate() {
        StuckTaskRuleCreation creation = this.creation();

        StuckTaskRule actual = this.client.create(creation);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getName()).isEqualTo(creation.getName());
        assertThat(actual.getProcedureKeyword()).isEqualTo("labour");
        assertThat(actual.getThresholdDays()).isEqualTo(20);
        assertThat(actual.getPenaltyAmount()).isEqualByComparingTo("75.50");
        assertThat(actual.getActive()).isTrue();
        assertThat(actual.getCreatedAt()).isEqualTo(LocalDate.now());
        assertThat(actual.getCreatedByUser().getId()).isEqualTo(USER_ID_0);
        assertThat(actual.getCreatedByUser().getMobile()).isEqualTo("600000100");
        assertThat(actual.getCreatedByUser().getFirstName()).isEqualTo("cliente0");
    }

    @Test
    void testCreateInactiveWithoutPenalty() {
        StuckTaskRuleCreation creation = this.creation();
        creation.setActive(false);
        creation.setPenaltyAmount(null);

        StuckTaskRule actual = this.client.create(creation);

        assertThat(actual.getActive()).isFalse();
        assertThat(actual.getPenaltyAmount()).isNull();
    }

    @Test
    void testCreateDuplicateName() {
        StuckTaskRuleCreation creation = this.creation();
        creation.setName("Tax procedure inactivity");
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateUnknownUser() {
        StuckTaskRuleCreation creation = this.creation();
        creation.setUserId(UNKNOWN_ID);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.NotFound.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidName(String name) {
        StuckTaskRuleCreation creation = this.creation();
        creation.setName(name);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutThresholdDays() {
        StuckTaskRuleCreation creation = this.creation();
        creation.setThresholdDays(null);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutUser() {
        StuckTaskRuleCreation creation = this.creation();
        creation.setUserId(null);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testFindAlertReport() {
        List<StuckTaskRuleAlertReport> report = this.client.findAlertReport();

        assertThat(report).extracting(StuckTaskRuleAlertReport::getTotalAlertCount)
                .isSortedAccordingTo(Comparator.reverseOrder());
        assertThat(report).filteredOn(item -> item.getRuleName().equals("Tax procedure inactivity"))
                .singleElement().satisfies(item -> {
                    assertThat(item.getTotalAlertCount()).isGreaterThanOrEqualTo(2);
                    assertThat(item.getUnresolvedAlertCount()).isGreaterThanOrEqualTo(1);
                    assertThat(item.getCreatedByUser().getId()).isEqualTo(USER_ID_0);
                    assertThat(item.getCreatedByUser().getFirstName()).isEqualTo("cliente0");
                });
        assertThat(report).filteredOn(item -> item.getRuleName().equals("Eviction procedure delay"))
                .singleElement().satisfies(item -> {
                    assertThat(item.getTotalAlertCount()).isGreaterThanOrEqualTo(2);
                    assertThat(item.getCreatedByUser().getId()).isEqualTo(USER_ID_1);
                });
        assertThat(report).filteredOn(item -> item.getRuleName().equals("Inheritance procedure backlog"))
                .singleElement().satisfies(item ->
                        assertThat(item.getTotalAlertCount()).isGreaterThanOrEqualTo(1));
    }
}
