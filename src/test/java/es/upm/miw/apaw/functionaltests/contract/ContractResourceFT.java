package es.upm.miw.apaw.functionaltests.contract;

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
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
        classes = ContractResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
public class ContractResourceFT {
    private static final UUID CLAUSE_ID_0 = UUID.fromString("cccccccc-dddd-eeee-ffff-000000000000");
    private static final UUID CLAUSE_ID_1 = UUID.fromString("cccccccc-dddd-eeee-ffff-000000000001");
    private static final UUID CONTRACT_ID_0 = UUID.fromString("dddddddd-eeee-ffff-aaaa-000000000000");
    private static final UUID CONTRACT_ID_1 = UUID.fromString("dddddddd-eeee-ffff-aaaa-000000000001");
    private static final UUID CONTRACT_ID_2 = UUID.fromString("dddddddd-eeee-ffff-aaaa-000000000002");
    private static final UUID CONTRACT_ID_5 = UUID.fromString("dddddddd-eeee-ffff-aaaa-000000000005");
    private static final UUID CONTRACT_ID_6 = UUID.fromString("dddddddd-eeee-ffff-aaaa-000000000006");
    private static final UUID CONTRACT_ID_9 = UUID.fromString("dddddddd-eeee-ffff-aaaa-000000000009");
    private static final UUID CONTRACT_ID_10 = UUID.fromString("dddddddd-eeee-ffff-aaaa-000000000010");
    private static final UUID CONTRACT_ID_11 = UUID.fromString("dddddddd-eeee-ffff-aaaa-000000000011");
    private static final UUID CONTRACT_ID_14 = UUID.fromString("dddddddd-eeee-ffff-aaaa-000000000014");
    private static final UUID CONTRACT_ID_17 = UUID.fromString("dddddddd-eeee-ffff-aaaa-000000000017");
    private static final UUID CONTRACT_ID_19 = UUID.fromString("dddddddd-eeee-ffff-aaaa-000000000019");
    private static final UUID USER_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");
    private static final UUID UNKNOWN_ID = UUID.fromString("cccccccc-dddd-eeee-ffff-999999999999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = ContractClient.class)
    static class ClientConfiguration { }

    @Autowired
    private ContractClient client;

    @Test
    void testCreate() {
        CreationContract creation = CreationContract.builder()
                .title("Contrato Feign")
                .type(ContractType.SERVICE)
                .startDate(LocalDate.of(2026, 10, 1))
                .endDate(LocalDate.of(2027, 10, 1))
                .amount(new BigDecimal("15000.00"))
                .automaticRenewal(true)
                .clauseIds(List.of(CLAUSE_ID_0, CLAUSE_ID_1))
                .userId(USER_ID)
                .build();

        Contract contract = this.client.create(creation);

        assertThat(contract.getId()).isNotNull();
        assertThat(contract.getTitle()).isEqualTo("Contrato Feign");
        assertThat(contract.getType()).isEqualTo(ContractType.SERVICE);
        assertThat(contract.getStartDate()).isEqualTo(LocalDate.of(2026, 10, 1));
        assertThat(contract.getEndDate()).isEqualTo(LocalDate.of(2027, 10, 1));
        assertThat(contract.getAmount()).isEqualByComparingTo("15000.00");
        assertThat(contract.getAutomaticRenewal()).isTrue();
    }

    @Test
    void testCreateUnknownClause() {
        CreationContract creation = CreationContract.builder()
                .title("Contrato cláusula desconocida " + UUID.randomUUID())
                .type(ContractType.SERVICE)
                .startDate(LocalDate.of(2026, 10, 1))
                .endDate(LocalDate.of(2027, 10, 1))
                .amount(new BigDecimal("15000.00"))
                .automaticRenewal(true)
                .clauseIds(List.of(UNKNOWN_ID))
                .userId(USER_ID)
                .build();

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateUnknownUser() {
        CreationContract creation = CreationContract.builder()
                .title("Contrato usuario desconocido " + UUID.randomUUID())
                .type(ContractType.SERVICE)
                .startDate(LocalDate.of(2026, 10, 1))
                .endDate(LocalDate.of(2027, 10, 1))
                .amount(new BigDecimal("15000.00"))
                .automaticRenewal(true)
                .clauseIds(List.of(CLAUSE_ID_0))
                .userId(UNKNOWN_ID)
                .build();

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidTitle(String title) {
        CreationContract creation = CreationContract.builder()
                .title(title)
                .startDate(LocalDate.of(2026, 10, 1))
                .clauseIds(List.of(CLAUSE_ID_0))
                .userId(USER_ID)
                .build();

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testFindAll() {
        assertThat(this.client.find(new ContractFindCriteria()))
                .extracting(Contract::getId)
                .contains(
                        CONTRACT_ID_0,
                        CONTRACT_ID_1,
                        CONTRACT_ID_9,
                        CONTRACT_ID_19
                );
    }

    @Test
    void testFindByTitle() {
        assertThat(this.client.find(
                ContractFindCriteria.builder()
                        .title("Contrato de servicios de consultoría")
                        .build()))
                .extracting(Contract::getId)
                .containsExactlyInAnyOrder(
                        CONTRACT_ID_0,
                        CONTRACT_ID_10
                );
    }

    @Test
    void testFindByActive() {
        assertThat(this.client.find(
                ContractFindCriteria.builder()
                        .active(false)
                        .build()))
                .extracting(Contract::getId)
                .contains(
                        CONTRACT_ID_6,
                        CONTRACT_ID_9,
                        CONTRACT_ID_10,
                        CONTRACT_ID_17,
                        CONTRACT_ID_19
                )
                .doesNotContain(CONTRACT_ID_0);
    }

    @Test
    void testFindByClauseType() {
        assertThat(this.client.find(
                ContractFindCriteria.builder()
                        .clauseType(ClauseType.DATA_PROTECTION)
                        .build()))
                .extracting(Contract::getId)
                .contains(
                        CONTRACT_ID_0,
                        CONTRACT_ID_5,
                        CONTRACT_ID_14
                );
    }

    @Test
    void testFindCombinedCriteria() {
        assertThat(this.client.find(
                ContractFindCriteria.builder()
                        .userCity("Sevilla")
                        .active(false)
                        .build()))
                .extracting(Contract::getId)
                .contains(
                        CONTRACT_ID_11,
                        CONTRACT_ID_14,
                        CONTRACT_ID_17
                )
                .doesNotContain(
                        CONTRACT_ID_2,
                        CONTRACT_ID_5
                );
    }

    @Test
    void testFindByUserCity() {
        assertThat(this.client.find(
                ContractFindCriteria.builder()
                        .userCity("Sevilla")
                        .build()))
                .extracting(Contract::getId)
                .contains(
                        CONTRACT_ID_2,
                        CONTRACT_ID_5,
                        CONTRACT_ID_11,
                        CONTRACT_ID_17
                )
                .doesNotContain(CONTRACT_ID_0);
    }

    @Test
    void testFindUnknownUserCity() {
        assertThat(this.client.find(
                ContractFindCriteria.builder()
                        .userCity("Barcelona")
                        .build()))
                .isEmpty();
    }
}