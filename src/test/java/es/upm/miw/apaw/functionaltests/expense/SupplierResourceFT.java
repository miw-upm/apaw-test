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

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = SupplierResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class SupplierResourceFT {

    private static final UUID SUPPLIER_0_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");
    private static final UUID SUPPLIER_1_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0001");
    private static final UUID UNKNOWN_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff9999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = SupplierClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private SupplierClient client;

    @Test
    void testCreateUpdateAndDelete() {
        Supplier supplier = this.client.create(Supplier.builder()
                .taxId("C" + UUID.randomUUID().toString().substring(0, 8))
                .companyName("Feign Supplier")
                .build());

        assertThat(supplier.getId()).isNotNull();
        assertThat(supplier.getCompanyName()).isEqualTo("Feign Supplier");

        supplier.setCompanyName("Feign Supplier Updated");
        supplier.setAddress("Updated Address");
        Supplier updated = this.client.update(supplier.getId(), supplier);
        assertThat(updated.getCompanyName()).isEqualTo("Feign Supplier Updated");
        assertThat(updated.getAddress()).isEqualTo("Updated Address");

        this.client.delete(supplier.getId());
        assertThatThrownBy(() -> this.client.read(supplier.getId())).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testRead() {
        Supplier supplier = this.client.read(SUPPLIER_0_ID);
        assertThat(supplier.getId()).isEqualTo(SUPPLIER_0_ID);
        assertThat(supplier.getTaxId()).isEqualTo("B12345678");
        assertThat(supplier.getCompanyName()).isEqualTo("Office Supplies Corp");
    }

    @Test
    void testFindAll() {
        assertThat(this.client.findAll()).extracting(Supplier::getCompanyName)
                .contains("Office Supplies Corp", "Global Travel Agency");
    }

    @Test
    void testFindExpenseReport() {
        assertThat(this.client.findExpenseReport())
                .filteredOn(report -> report.getCompanyName().equals("Office Supplies Corp"))
                .singleElement().satisfies(report -> {
                    assertThat(report.getTotalExpenses()).isGreaterThanOrEqualTo(2);
                    assertThat(report.getTotalAmount()).isNotNull();
                });
    }

    @Test
    void testCreateDuplicateTaxId() {
        Supplier supplier = Supplier.builder().taxId("B12345678").companyName("Duplicate Tax").build();
        assertThatThrownBy(() -> this.client.create(supplier)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testReadNotFound() {
        assertThatThrownBy(() -> this.client.read(UNKNOWN_ID)).isInstanceOf(FeignException.NotFound.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidTaxId(String taxId) {
        Supplier supplier = Supplier.builder().taxId(taxId).companyName("Invalid Tax").build();
        assertThatThrownBy(() -> this.client.create(supplier)).isInstanceOf(FeignException.BadRequest.class);
    }
}