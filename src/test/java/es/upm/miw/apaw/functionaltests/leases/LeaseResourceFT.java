package es.upm.miw.apaw.functionaltests.leases;

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

@SpringBootTest(classes = LeaseResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class LeaseResourceFT {
    private static final UUID RESIDENTIAL_ID = UUID.fromString("eeeeeeee-ffff-aaaa-bbbb-ccccdddd0000");
    private static final UUID COMMERCIAL_ID = UUID.fromString("eeeeeeee-ffff-aaaa-bbbb-ccccdddd0001");
    private static final UUID ENDED_ID = UUID.fromString("eeeeeeee-ffff-aaaa-bbbb-ccccdddd0002");
    private static final UUID USED_AMENDMENT_ID = UUID.fromString("dddddddd-eeee-ffff-aaaa-bbbbcccc0000");
    private static final UUID UNKNOWN_ID = UUID.fromString("dddddddd-eeee-ffff-aaaa-bbbbcccc9999");
    private static final UUID USER_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0008");
    private static final String USER_MOBILE = "600000108";

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = {LeaseClient.class, AmendmentClient.class})
    static class ClientConfiguration {
    }

    @Autowired
    private LeaseClient client;
    @Autowired
    private AmendmentClient amendmentClient;

    private CreationLease creation() {
        // No hay DELETE de arrendamientos: se usa un número único en cada ejecución.
        return CreationLease.builder().leaseNumber("FT-" + UUID.randomUUID())
                .propertyAddress("Calle Feign 1, Madrid").startDate(LocalDate.now().minusMonths(1))
                .monthlyRent(new BigDecimal("700.00")).leaseType(LeaseType.RESIDENTIAL)
                .amendmentIds(List.of()).userId(USER_ID).build();
    }

    private Amendment createAmendment(AmendmentType amendmentType) {
        return this.amendmentClient.create(Amendment.builder().amendmentNumber(1)
                .description("Feign lease amendment").effectiveDate(LocalDate.now())
                .amendmentType(amendmentType).build());
    }

    @Test
    void testCreate() {
        Amendment amendment = this.createAmendment(AmendmentType.TERM_EXTENSION);
        CreationLease creation = this.creation();
        creation.setCadastralReference("FT" + UUID.randomUUID());
        creation.setAmendmentIds(List.of(amendment.getId()));

        Lease actual = this.client.create(creation);
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getLeaseNumber()).isEqualTo(creation.getLeaseNumber());
        assertThat(actual.getMonthlyRent()).isEqualByComparingTo(creation.getMonthlyRent());
        assertThat(actual.getActive()).isTrue();
        assertThat(actual.getCreatedAt()).isNotNull();
        assertThat(actual.getAmendments()).extracting(Amendment::getId).containsExactly(amendment.getId());
        assertThat(actual.getUserSnapshot().getId()).isEqualTo(USER_ID);
        assertThat(actual.getUserSnapshot().getMobile()).isEqualTo(USER_MOBILE);
        assertThat(this.client.find(LeaseFindCriteria.builder().userMobile(USER_MOBILE).build()))
                .extracting(Lease::getId).contains(actual.getId());
        assertThatThrownBy(() -> this.amendmentClient.delete(amendment.getId()))
                .isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateWithoutAmendments() {
        CreationLease creation = this.creation();
        creation.setAmendmentIds(null);
        assertThat(this.client.create(creation).getAmendments()).isEmpty();
    }

    @Test
    void testFindAll() {
        assertThat(this.client.find(new LeaseFindCriteria())).extracting(Lease::getId)
                .contains(RESIDENTIAL_ID, COMMERCIAL_ID, ENDED_ID);
    }

    @Test
    void testFindByUserMobileReturnsSummary() {
        assertThat(this.client.find(LeaseFindCriteria.builder().userMobile("600000106").build()))
                .singleElement().satisfies(lease -> {
                    assertThat(lease.getId()).isEqualTo(COMMERCIAL_ID);
                    assertThat(lease.getLeaseNumber()).isEqualTo("LSE-2024-0002");
                    assertThat(lease.getMonthlyRent()).isEqualByComparingTo("2100.00");
                    assertThat(lease.getAmendments()).isNull();
                    assertThat(lease.getUserSnapshot().getMobile()).isEqualTo("600000106");
                    assertThat(lease.getUserSnapshot().getFirstName()).isEqualTo("Cliente6");
                });
    }

    @Test
    void testFindByLeaseType() {
        assertThat(this.client.find(LeaseFindCriteria.builder().leaseType(LeaseType.SEASONAL).build()))
                .extracting(Lease::getId).contains(ENDED_ID).doesNotContain(RESIDENTIAL_ID, COMMERCIAL_ID);
    }

    @Test
    void testFindByInForce() {
        assertThat(this.client.find(LeaseFindCriteria.builder().inForce(true).build()))
                .extracting(Lease::getId).contains(COMMERCIAL_ID).doesNotContain(ENDED_ID);
        assertThat(this.client.find(LeaseFindCriteria.builder().inForce(false).build()))
                .extracting(Lease::getId).contains(ENDED_ID).doesNotContain(COMMERCIAL_ID);
    }

    @Test
    void testFindByAmendmentType() {
        assertThat(this.client.find(LeaseFindCriteria.builder().amendmentType(AmendmentType.SCOPE_CHANGE).build()))
                .extracting(Lease::getId).contains(COMMERCIAL_ID).doesNotContain(RESIDENTIAL_ID, ENDED_ID);
    }

    @Test
    void testFindCombinedCriteria() {
        assertThat(this.client.find(LeaseFindCriteria.builder().userMobile("600000105")
                .leaseType(LeaseType.RESIDENTIAL).amendmentType(AmendmentType.PRICE_CHANGE).build()))
                .extracting(Lease::getId).containsExactly(RESIDENTIAL_ID);
    }

    @Test
    void testFindUnknownMobile() {
        assertThat(this.client.find(LeaseFindCriteria.builder().userMobile("699999998").build())).isEmpty();
    }

    @Test
    void testFindAmendmentReport() {
        List<LeaseAmendmentReport> report = this.client.findAmendmentReport();
        assertThat(report).extracting(LeaseAmendmentReport::getTotalAdditionalAmount)
                .isSortedAccordingTo(Comparator.reverseOrder());
        assertThat(report).filteredOn(item -> item.getLeaseType() == LeaseType.COMMERCIAL)
                .singleElement().satisfies(item -> {
                    assertThat(item.getApprovedAmendmentCount()).isGreaterThanOrEqualTo(1);
                    assertThat(item.getTotalAdditionalAmount()).isGreaterThanOrEqualTo(new BigDecimal("60.00"));
                });
    }

    @Test
    void testCreateDuplicateLeaseNumber() {
        CreationLease creation = this.creation();
        creation.setLeaseNumber("LSE-2024-0001");
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateDuplicateCadastralReference() {
        CreationLease creation = this.creation();
        creation.setCadastralReference("9872023VH5797S0001WX");
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateAmendmentOfOtherLease() {
        CreationLease creation = this.creation();
        creation.setAmendmentIds(List.of(USED_AMENDMENT_ID));
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreateUnknownAmendment() {
        CreationLease creation = this.creation();
        creation.setAmendmentIds(List.of(UNKNOWN_ID));
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateRepeatedAmendment() {
        Amendment amendment = this.createAmendment(AmendmentType.OTHER);
        CreationLease creation = this.creation();
        creation.setAmendmentIds(List.of(amendment.getId(), amendment.getId()));
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateUnknownUser() {
        CreationLease creation = this.creation();
        creation.setUserId(UNKNOWN_ID);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.NotFound.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidLeaseNumber(String leaseNumber) {
        CreationLease creation = this.creation();
        creation.setLeaseNumber(leaseNumber);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutMonthlyRent() {
        CreationLease creation = this.creation();
        creation.setMonthlyRent(null);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutUser() {
        CreationLease creation = this.creation();
        creation.setUserId(null);
        assertThatThrownBy(() -> this.client.create(creation)).isInstanceOf(FeignException.BadRequest.class);
    }
}
