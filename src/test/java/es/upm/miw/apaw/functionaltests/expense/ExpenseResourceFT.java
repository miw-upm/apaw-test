package es.upm.miw.apaw.functionaltests.expense;

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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = ExpenseResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class ExpenseResourceFT {

    private static final UUID SUPPLIER_0_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");
    private static final UUID USER_0_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");
    private static final UUID UNKNOWN_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff9999");

    private static final UUID EXPENSE_0_ID = UUID.fromString("bbbbbbbb-cccc-dddd-eeee-ffffffff0000");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = ExpenseClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private ExpenseClient client;

    private CreationExpense creation() {
        return CreationExpense.builder()
                .reference("EXP-FEIGN-" + UUID.randomUUID())
                .amount(new BigDecimal("250.00"))
                .description("Feign Expense")
                .category("Office")
                .supplierId(SUPPLIER_0_ID)
                .applicantId(USER_0_ID)
                .build();
    }

    @Test
    void testCreate() {
        CreationExpense creation = this.creation();
        Expense actual = this.client.create(creation);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getReference()).isEqualTo(creation.getReference());
        assertThat(actual.getAmount()).isEqualByComparingTo(creation.getAmount());
        assertThat(actual.getExpenseDate()).isEqualTo(LocalDate.now());
        assertThat(actual.getIsPaid()).isFalse();
        assertThat(actual.getSupplier().getId()).isEqualTo(creation.getSupplierId());
        assertThat(actual.getUserSnapshot().getId()).isEqualTo(creation.getApplicantId());
    }

    @Test
    void testFindAll() {
        assertThat(this.client.find(new ExpenseFindCriteria())).extracting(Expense::getId)
                .contains(EXPENSE_0_ID);
    }

    @Test
    void testFindByCategory() {
        assertThat(this.client.find(ExpenseFindCriteria.builder().category("Office").build()))
                .extracting(Expense::getId).contains(EXPENSE_0_ID);
    }

    @Test
    void testFindByUnpaid() {
        assertThat(this.client.find(ExpenseFindCriteria.builder().unpaid(true).build()))
                .extracting(Expense::getId).contains(EXPENSE_0_ID);
    }

    @Test
    void testFindBySupplierTaxId() {
        assertThat(this.client.find(ExpenseFindCriteria.builder().supplierTaxId("B12345678").build()))
                .extracting(Expense::getId).contains(EXPENSE_0_ID);
    }

    @Test
    void testCreateDuplicateReference() {
        CreationExpense creation = this.creation();
        creation.setReference("EXP-SEED-001");
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateUnknownSupplier() {
        CreationExpense creation = this.creation();
        creation.setSupplierId(UNKNOWN_ID);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.NotFound.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidReference(String reference) {
        CreationExpense creation = this.creation();
        creation.setReference(reference);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutAmount() {
        CreationExpense creation = this.creation();
        creation.setAmount(null);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }
}