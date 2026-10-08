package es.upm.miw.apaw.functionaltests.secondlawchance;

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
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest(
        classes = DebtResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
public class DebtResourceFT {
    private static final String DEBT_PREFIX = "99999999-aaaa-bbbb-cccc-ddddeeee";
    private static final UUID DEBT_ID_0 = UUID.fromString(DEBT_PREFIX + "0000");
    private static final UUID DEBT_ID_1 = UUID.fromString(DEBT_PREFIX + "0001");
    private static final UUID DEBT_ID_2 = UUID.fromString(DEBT_PREFIX + "0002");
    private static final UUID DEBT_ID_3 = UUID.fromString(DEBT_PREFIX + "0003");
    private static final UUID DEBT_ID_4 = UUID.fromString(DEBT_PREFIX + "0004");
    private static final UUID DEBT_ID_5 = UUID.fromString(DEBT_PREFIX + "0005");
    private static final UUID USER_ID_0 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");
    private static final UUID USER_ID_1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0001");
    private static final UUID UNKNOWN_ID = UUID.fromString("99999999-aaaa-bbbb-cccc-999999999999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = DebtClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private DebtClient client;

    private Debt newDebt() {
        return Debt.builder()
                .contractNumber("FT-" + UUID.randomUUID())
                .issueDate(LocalDate.of(2024, 7, 1))
                .creditorName("Acreedor Feign")
                .amount(new BigDecimal("1200.50"))
                .type(CreditorType.PUBLIC)
                .guarantee(true)
                .build();
    }

    @Test
    void testRead() {
        Debt debt = this.client.read(DEBT_ID_0);

        assertThat(debt.getId()).isEqualTo(DEBT_ID_0);
        assertThat(debt.getContractNumber()).isEqualTo("AEAT-2023-0001");
        assertThat(debt.getIssueDate()).isEqualTo(LocalDate.of(2023, 3, 15));
        assertThat(debt.getCreditorName()).isEqualTo("Agencia Tributaria");
        assertThat(debt.getAmount()).isEqualByComparingTo("4850.20");
        assertThat(debt.getType()).isEqualTo(CreditorType.PUBLIC);
        assertThat(debt.getGuarantee()).isFalse();
    }

    @Test
    void testReadNotFound() {
        assertThatThrownBy(() -> this.client.read(UNKNOWN_ID))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testFindAll() {
        assertThat(this.client.findAll())
                .extracting(Debt::getId)
                .contains(DEBT_ID_0, DEBT_ID_1, DEBT_ID_2, DEBT_ID_3, DEBT_ID_4, DEBT_ID_5);
    }

    @Test
    void testCreateUpdateAndDelete() {
        Debt created = this.client.create(this.newDebt());

        assertThat(created.getId()).isNotNull();
        assertThat(created.getCreditorName()).isEqualTo("Acreedor Feign");
        assertThat(created.getAmount()).isEqualByComparingTo("1200.50");
        assertThat(created.getType()).isEqualTo(CreditorType.PUBLIC);
        assertThat(created.getGuarantee()).isTrue();

        created.setCreditorName("Acreedor Feign actualizado");
        created.setAmount(new BigDecimal("999.99"));
        created.setType(CreditorType.PRIVATE);
        created.setGuarantee(false);

        Debt updated = this.client.update(created.getId(), created);

        assertThat(updated.getId()).isEqualTo(created.getId());
        assertThat(updated.getCreditorName()).isEqualTo("Acreedor Feign actualizado");
        assertThat(updated.getAmount()).isEqualByComparingTo("999.99");
        assertThat(updated.getType()).isEqualTo(CreditorType.PRIVATE);
        assertThat(updated.getGuarantee()).isFalse();
        assertThat(this.client.read(created.getId()).getCreditorName()).isEqualTo("Acreedor Feign actualizado");

        this.client.delete(created.getId());

        assertThatThrownBy(() -> this.client.read(created.getId()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateDefaults() {
        Debt debt = this.newDebt();
        debt.setType(null);
        debt.setGuarantee(null);

        Debt created = this.client.create(debt);

        assertThat(created.getType()).isEqualTo(CreditorType.PRIVATE);
        assertThat(created.getGuarantee()).isFalse();

        this.client.delete(created.getId());
    }

    @Test
    void testCreateRepeatedContractNumber() {
        Debt debt = this.newDebt();
        debt.setContractNumber("AEAT-2023-0001");

        assertThatThrownBy(() -> this.client.create(debt))
                .isInstanceOf(FeignException.Conflict.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidContractNumber(String contractNumber) {
        Debt debt = this.newDebt();
        debt.setContractNumber(contractNumber);

        assertThatThrownBy(() -> this.client.create(debt))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutAmount() {
        Debt debt = this.newDebt();
        debt.setAmount(null);

        assertThatThrownBy(() -> this.client.create(debt))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testUpdateNotFound() {
        assertThatThrownBy(() -> this.client.update(UNKNOWN_ID, this.newDebt()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testUpdateRepeatedContractNumber() {
        Debt created = this.client.create(this.newDebt());
        created.setContractNumber("AEAT-2023-0001");

        assertThatThrownBy(() -> this.client.update(created.getId(), created))
                .isInstanceOf(FeignException.Conflict.class);

        this.client.delete(created.getId());
    }

    @Test
    void testPatch() {
        Debt created = this.client.create(this.newDebt());

        Debt patched = this.client.patch(created.getId(),
                new DebtPatch(null, null, "Acreedor parcheado", null, CreditorType.PRIVATE, null));

        assertThat(patched.getCreditorName()).isEqualTo("Acreedor parcheado");
        assertThat(patched.getType()).isEqualTo(CreditorType.PRIVATE);
        assertThat(patched.getContractNumber()).isEqualTo(created.getContractNumber());
        assertThat(patched.getAmount()).isEqualByComparingTo("1200.50");
        assertThat(patched.getGuarantee()).isTrue();
        assertThat(this.client.read(created.getId())).isEqualTo(patched);

        this.client.delete(created.getId());
    }

    @Test
    void testPatchNotFound() {
        DebtPatch patch = new DebtPatch(null, null, "Acreedor", null, null, null);

        assertThatThrownBy(() -> this.client.patch(UNKNOWN_ID, patch))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testPatchBlankCreditorName() {
        DebtPatch patch = new DebtPatch(null, null, " ", null, null, null);

        assertThatThrownBy(() -> this.client.patch(DEBT_ID_1, patch))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testPatchRepeatedContractNumber() {
        DebtPatch patch = new DebtPatch("AEAT-2023-0001", null, null, null, null, null);

        assertThatThrownBy(() -> this.client.patch(DEBT_ID_1, patch))
                .isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testDeleteReferencedDebt() {
        assertThatThrownBy(() -> this.client.delete(DEBT_ID_0))
                .isInstanceOf(FeignException.Conflict.class);
        assertThat(this.client.read(DEBT_ID_0).getId()).isEqualTo(DEBT_ID_0);
    }

    @Test
    void testSharedReport() {
        List<SharedDebtReport> report = this.client.findSharedReport();

        SharedDebtReport shared = report.stream()
                .filter(item -> DEBT_ID_0.equals(item.debtId()))
                .findFirst()
                .orElseThrow();
        assertThat(shared.contractNumber()).isEqualTo("AEAT-2023-0001");
        assertThat(shared.creditorName()).isEqualTo("Agencia Tributaria");
        assertThat(shared.amount()).isEqualByComparingTo("4850.20");
        assertThat(shared.type()).isEqualTo(CreditorType.PUBLIC);
        assertThat(shared.caseCount()).isGreaterThanOrEqualTo(2);
        assertThat(shared.debtorCount()).isGreaterThanOrEqualTo(2);
        assertThat(shared.debtorIds()).contains(USER_ID_0, USER_ID_1);
        assertThat(shared.debtors())
                .extracting(UserSnapshot::getId, UserSnapshot::getMobile, UserSnapshot::getFirstName)
                .contains(
                        tuple(USER_ID_0, "600000100", "cliente0"),
                        tuple(USER_ID_1, "600000101", "cliente1")
                );
    }

    @Test
    void testSharedReportExcludesUnsharedDebts() {
        assertThat(this.client.findSharedReport())
                .extracting(SharedDebtReport::debtId)
                .doesNotContain(DEBT_ID_2, DEBT_ID_3, DEBT_ID_4, DEBT_ID_5);
    }

    @Test
    void testSharedReportOnlyHasMultipleDebtors() {
        assertThat(this.client.findSharedReport())
                .allSatisfy(item -> assertThat(item.debtorCount()).isGreaterThan(1));
    }
}
