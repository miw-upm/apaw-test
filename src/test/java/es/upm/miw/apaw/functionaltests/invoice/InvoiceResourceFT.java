package es.upm.miw.apaw.functionaltests.invoice;

import es.upm.miw.apaw.functionaltests.user.UserClient;
import es.upm.miw.apaw.functionaltests.user.UserDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(
        classes = InvoiceResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
class InvoiceResourceFT {

    private static final UUID INVOICE_001 =
            UUID.fromString("b1234567-bbbb-cccc-dddd-eeeeffff0000");
    private static final UUID INVOICE_002 =
            UUID.fromString("b1234567-bbbb-cccc-dddd-eeeeffff0001");
    private static final UUID INVOICE_003 =
            UUID.fromString("b1234567-bbbb-cccc-dddd-eeeeffff0002");
    private static final UUID INVOICE_004 =
            UUID.fromString("b1234567-bbbb-cccc-dddd-eeeeffff0003");
    private static final UUID INVOICE_005 =
            UUID.fromString("b1234567-bbbb-cccc-dddd-eeeeffff0004");
    private static final UUID INVOICE_006 =
            UUID.fromString("b1234567-bbbb-cccc-dddd-eeeeffff0005");

    private static final UUID SERVICE_0 =
            UUID.fromString("a1234567-bbbb-cccc-dddd-eeeeffff0000");
    private static final UUID SERVICE_1 =
            UUID.fromString("a1234567-bbbb-cccc-dddd-eeeeffff0001");

    // Usuario conocido de los tests de integración.
    // Confirma que existe en apaw-user antes de ejecutar testCreate.
    private static final UUID USER_ID =
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = {
            InvoiceClient.class,
            UserClient.class
    })
    static class ClientConfiguration {
    }

    @Autowired
    private InvoiceClient invoiceClient;

    @Autowired
    private UserClient userClient;

    @Test
    void testFindByPaid() {
        List<Invoice> invoices = this.invoiceClient.findByCriteria(
                InvoiceFindCriteria.builder()
                        .paid(true)
                        .build()
        );

        assertThat(invoices)
                .extracting(Invoice::getId)
                .contains(INVOICE_001, INVOICE_002, INVOICE_004);

        assertThat(invoices)
                .allMatch(invoice -> Boolean.TRUE.equals(invoice.getPaid()));
    }

    @Test
    void testFindByServiceName() {
        List<Invoice> invoices = this.invoiceClient.findByCriteria(
                InvoiceFindCriteria.builder()
                        .serviceName("Initial Legal Consultation")
                        .build()
        );

        assertThat(invoices)
                .extracting(Invoice::getId)
                .contains(INVOICE_001, INVOICE_002, INVOICE_003);

        assertThat(invoices)
                .allMatch(invoice -> invoice.getServices().stream()
                        .anyMatch(service -> "Initial Legal Consultation"
                                .equals(service.getName())));
    }

    @Test
    void testFindByIssueYear() {
        List<Invoice> invoices = this.invoiceClient.findByCriteria(
                InvoiceFindCriteria.builder()
                        .issueYear(2026)
                        .build()
        );

        assertThat(invoices)
                .extracting(Invoice::getId)
                .contains(
                        INVOICE_001, INVOICE_002, INVOICE_003,
                        INVOICE_004, INVOICE_005, INVOICE_006
                );

        assertThat(invoices)
                .allMatch(invoice -> invoice.getIssueDate().getYear() == 2026);
    }

    @Test
    void testFindByCombinedCriteria() {
        List<Invoice> invoices = this.invoiceClient.findByCriteria(
                InvoiceFindCriteria.builder()
                        .paid(true)
                        .issueYear(2026)
                        .serviceName("Initial Legal Consultation")
                        .build()
        );

        assertThat(invoices)
                .extracting(Invoice::getId)
                .contains(INVOICE_001, INVOICE_002);

        assertThat(invoices)
                .allMatch(invoice -> Boolean.TRUE.equals(invoice.getPaid()))
                .allMatch(invoice -> invoice.getIssueDate().getYear() == 2026);
    }

    @Test
    void testFindWithoutCriteria() {
        List<Invoice> invoices = this.invoiceClient.findByCriteria(
                InvoiceFindCriteria.builder().build()
        );

        assertThat(invoices)
                .extracting(Invoice::getId)
                .contains(
                        INVOICE_001, INVOICE_002, INVOICE_003,
                        INVOICE_004, INVOICE_005, INVOICE_006
                );
    }

    @Test
    void testFindLegalServiceInvoiceReport() {
        List<LegalServiceInvoiceReport> report =
                this.invoiceClient.findLegalServiceInvoiceReport();

        assertThat(report)
                .extracting(LegalServiceInvoiceReport::getServiceName)
                .contains(
                        "Initial Legal Consultation",
                        "Criminal Law Consultation",
                        "Contract Drafting"
                );

        LegalServiceInvoiceReport consultation = report.stream()
                .filter(item -> "Initial Legal Consultation"
                        .equals(item.getServiceName()))
                .findFirst()
                .orElseThrow();

        assertThat(consultation.getTotalInvoiceCount())
                .isGreaterThanOrEqualTo(3L);
        assertThat(consultation.getPaidInvoiceCount())
                .isGreaterThanOrEqualTo(2L);
    }

    @Test
    void testCreate() {
        UserDto user = this.userClient.readById(USER_ID);

        assertThat(user).isNotNull();

        CreationInvoice creation = CreationInvoice.builder()
                .vatRate(new BigDecimal("0.21"))
                .legalServiceIds(List.of(SERVICE_0, SERVICE_1))
                .userId(USER_ID)
                .build();

        Invoice invoice = this.invoiceClient.create(creation);

        assertThat(invoice.getId()).isNotNull();
        assertThat(invoice.getInvoiceNumber()).isNotBlank();
        assertThat(invoice.getIssueDate()).isNotNull();
        assertThat(invoice.getVatRate()).isEqualByComparingTo("0.21");
        assertThat(invoice.getPaid()).isFalse();
        assertThat(invoice.getServices())
                .extracting(LegalService::getId)
                .contains(SERVICE_0, SERVICE_1);
    }
}
