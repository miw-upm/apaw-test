package es.upm.miw.apaw.functionaltests.immigrationissues;

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
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

@SpringBootTest(classes = ImmigrationIssueResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class ImmigrationIssueResourceFT {
    private static final String APAW_USER_PREFIX = "aaaaaaaa-bbbb-cccc-dddd-eeeeffff";
    private static final String LAW_BASIS_PREFIX = "eeeeeeee-ffff-1111-2222-33334444";
    private static final String ISSUE_PREFIX = "eeeeeeee-ffff-1111-2222-55556666";

    private static final UUID USER_ID_0 = UUID.fromString(APAW_USER_PREFIX + "0000");
    private static final UUID USER_ID_4 = UUID.fromString(APAW_USER_PREFIX + "0004");
    private static final UUID UNKNOWN_ID = UUID.fromString(APAW_USER_PREFIX + "9999");
    private static final UUID LAW_BASIS_0 = UUID.fromString(LAW_BASIS_PREFIX + "0000");
    private static final UUID LAW_BASIS_1 = UUID.fromString(LAW_BASIS_PREFIX + "0001");
    private static final UUID ISSUE_ID_0 = UUID.fromString(ISSUE_PREFIX + "0000");
    private static final UUID ISSUE_ID_1 = UUID.fromString(ISSUE_PREFIX + "0001");
    private static final UUID ISSUE_ID_2 = UUID.fromString(ISSUE_PREFIX + "0002");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = ImmigrationIssueClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private ImmigrationIssueClient client;

    private CreationImmigrationIssue creation(List<UUID> lawBasisIds) {
        return CreationImmigrationIssue.builder()
                .subject("Immigration issue " + UUID.randomUUID())
                .clientNationality("Colombia")
                .clientImmigrationStatus("Permiso en vigor")
                .responseDueDate(LocalDate.of(2026, 6, 1))
                .lawBasisIds(lawBasisIds)
                .userId(USER_ID_0)
                .build();
    }

    private ImmigrationIssue create(CreationImmigrationIssue creation) {
        return this.client.create(creation);
    }

    @Test
    void testCreate() {
        CreationImmigrationIssue creation = this.creation(Arrays.asList(LAW_BASIS_0, LAW_BASIS_1));
        ImmigrationIssue actual = this.create(creation);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getSubject()).isEqualTo(creation.getSubject());
        assertThat(actual.getClientNationality()).isEqualTo(creation.getClientNationality());
        assertThat(actual.getClientImmigrationStatus()).isEqualTo(creation.getClientImmigrationStatus());
        assertThat(actual.getResponseDueDate()).isEqualTo(creation.getResponseDueDate());
        assertThat(actual.getOpenedAt()).isNotNull();
        assertThat(actual.getLawBases()).extracting(LawBasis::getId)
                .containsExactly(LAW_BASIS_0, LAW_BASIS_1);
        assertThat(actual.getLawBases()).extracting(LawBasis::getLawCode)
                .containsExactly("ES-LB-001", "ES-LB-002");
        assertThat(actual.getUserSnapshot().getId()).isEqualTo(USER_ID_0);
        assertThat(actual.getUserSnapshot().getMobile()).isEqualTo("600000100");
        assertThat(actual.getUserSnapshot().getFirstName()).isEqualTo("cliente0");
        assertThat(this.client.find(ImmigrationIssueFindCriteria.builder()
                        .clientNationality(creation.getClientNationality()).build()))
                .extracting(ImmigrationIssue::getId).contains(actual.getId());
    }

    @Test
    void testCreateAppliesDefaultEstimatedCost() {
        ImmigrationIssue actual = this.create(this.creation(Arrays.asList(LAW_BASIS_0)));
        assertThat(actual.getEstimatedCost()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void testCreateKeepsProvidedEstimatedCost() {
        CreationImmigrationIssue creation = this.creation(Arrays.asList(LAW_BASIS_0));
        creation.setEstimatedCost(new BigDecimal("725.50"));
        assertThat(this.create(creation).getEstimatedCost())
                .isEqualByComparingTo(new BigDecimal("725.50"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidSubject(String subject) {
        CreationImmigrationIssue creation = this.creation(Arrays.asList(LAW_BASIS_0));
        creation.setSubject(subject);
        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidClientNationality(String clientNationality) {
        CreationImmigrationIssue creation = this.creation(Arrays.asList(LAW_BASIS_0));
        creation.setClientNationality(clientNationality);
        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateMissingResponseDueDate() {
        CreationImmigrationIssue creation = this.creation(Arrays.asList(LAW_BASIS_0));
        creation.setResponseDueDate(null);
        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateMissingUserId() {
        CreationImmigrationIssue creation = this.creation(Arrays.asList(LAW_BASIS_0));
        creation.setUserId(null);
        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateEmptyLawBasisIds() {
        assertThatThrownBy(() -> this.client.create(this.creation(List.of())))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateMissingLawBasisIdInList() {
        assertThatThrownBy(() -> this.client.create(this.creation(Arrays.asList(LAW_BASIS_0, null))))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateLawBasisNotFound() {
        assertThatThrownBy(() -> this.client.create(this.creation(Arrays.asList(LAW_BASIS_0, UNKNOWN_ID))))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateUserNotFound() {
        CreationImmigrationIssue creation = this.creation(Arrays.asList(LAW_BASIS_0));
        creation.setUserId(UNKNOWN_ID);
        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateDuplicateSubject() {
        CreationImmigrationIssue creation = this.creation(Arrays.asList(LAW_BASIS_0));
        this.create(creation);
        CreationImmigrationIssue repeated = this.creation(Arrays.asList(LAW_BASIS_1));
        repeated.setSubject(creation.getSubject());
        assertThatThrownBy(() -> this.client.create(repeated))
                .isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testFindAll() {
        assertThat(this.client.find(new ImmigrationIssueFindCriteria()))
                .extracting(ImmigrationIssue::getId)
                .contains(ISSUE_ID_0, ISSUE_ID_1, ISSUE_ID_2);
    }

    @Test
    void testFindWithoutCriteriaIsSortedBySubject() {
        ImmigrationIssue created = this.create(this.creation(Arrays.asList(LAW_BASIS_0)));
        List<ImmigrationIssue> issues = this.client.find(new ImmigrationIssueFindCriteria());

        assertThat(issues).extracting(ImmigrationIssue::getId).contains(created.getId());
        assertThat(issues).extracting(ImmigrationIssue::getSubject)
                .isSortedAccordingTo(Comparator.naturalOrder());
    }

    @Test
    void testFindReturnsSummaryWithoutLawBases() {
        List<ImmigrationIssue> issues = this.client.find(
                ImmigrationIssueFindCriteria.builder().clientNationality("Colombia").build());

        assertThat(issues).isNotEmpty().allSatisfy(issue -> {
            assertThat(issue.getLawBases()).isNull();
            assertThat(issue.getUserSnapshot()).isNotNull();
        });
    }

    @Test
    void testFindByClientNationality() {
        String clientNationality = "N" + UUID.randomUUID();
        CreationImmigrationIssue creation = this.creation(Arrays.asList(LAW_BASIS_0));
        creation.setClientNationality(clientNationality);
        ImmigrationIssue created = this.create(creation);

        assertThat(this.client.find(ImmigrationIssueFindCriteria.builder()
                        .clientNationality(clientNationality).build()))
                .extracting(ImmigrationIssue::getId).containsExactly(created.getId());
    }

    @Test
    void testFindByClientNationalityWithoutMatches() {
        assertThat(this.client.find(ImmigrationIssueFindCriteria.builder()
                        .clientNationality("N" + UUID.randomUUID()).build()))
                .isEmpty();
    }

    @Test
    void testFindOverdue() {
        CreationImmigrationIssue past = this.creation(Arrays.asList(LAW_BASIS_0));
        past.setResponseDueDate(LocalDate.of(2000, 1, 1));
        ImmigrationIssue overdue = this.create(past);

        assertThat(this.client.find(ImmigrationIssueFindCriteria.builder().overdue(true).build()))
                .extracting(ImmigrationIssue::getId).contains(overdue.getId());
    }

    @Test
    void testFindNotOverdue() {
        CreationImmigrationIssue future = this.creation(Arrays.asList(LAW_BASIS_0));
        future.setResponseDueDate(LocalDate.of(2099, 1, 1));
        ImmigrationIssue notOverdue = this.create(future);

        assertThat(this.client.find(ImmigrationIssueFindCriteria.builder().overdue(false).build()))
                .extracting(ImmigrationIssue::getId).contains(notOverdue.getId());
    }

    @Test
    void testFindByLawName() {
        ImmigrationIssue created = this.create(this.creation(Arrays.asList(LAW_BASIS_0, LAW_BASIS_1)));

        assertThat(this.client.find(ImmigrationIssueFindCriteria.builder().lawName("4/2000").build()))
                .extracting(ImmigrationIssue::getId)
                .contains(created.getId())
                .doesNotContain(ISSUE_ID_2);
    }

    @Test
    void testFindByLawNameIsCaseInsensitiveAndPartial() {
        ImmigrationIssue created = this.create(this.creation(Arrays.asList(LAW_BASIS_0)));

        assertThat(this.client.find(ImmigrationIssueFindCriteria.builder().lawName("ley org").build()))
                .extracting(ImmigrationIssue::getId).contains(created.getId());
    }

    @Test
    void testFindByFamilyName() {
        CreationImmigrationIssue creation = this.creation(Arrays.asList(LAW_BASIS_0));
        creation.setUserId(USER_ID_4);
        ImmigrationIssue created = this.create(creation);

        List<ImmigrationIssue> issues = this.client.find(
                ImmigrationIssueFindCriteria.builder().familyName("Romero Navarro").build());

        assertThat(issues).extracting(ImmigrationIssue::getId)
                .contains(created.getId())
                .doesNotContain(ISSUE_ID_0);
        assertThat(issues).extracting(issue -> issue.getUserSnapshot().getFirstName())
                .containsOnly("cliente4");
    }

    @Test
    void testFindByFamilyNameWithoutMatches() {
        assertThat(this.client.find(ImmigrationIssueFindCriteria.builder()
                        .familyName("Nadie").build()))
                .isEmpty();
    }

    @Test
    void testFindUsageReport() {
        this.create(this.creation(Arrays.asList(LAW_BASIS_0)));
        List<LawBasisUsageReport> reports = this.client.findUsageReport();

        assertThat(reports).isNotEmpty();
        assertThat(reports).extracting(LawBasisUsageReport::getLawCode).contains("ES-LB-001");
        assertThat(reports).allSatisfy(report -> {
            assertThat(report.getLawCode()).isNotBlank();
            assertThat(report.getTotalIssues()).isGreaterThanOrEqualTo(1);
        });
        assertThat(reports).isSortedAccordingTo(
                Comparator.comparingLong(LawBasisUsageReport::getTotalIssues).reversed());
    }

    @Test
    void testFindUsageReportCombinesStatusAndLawCode() {
        this.create(this.creation(Arrays.asList(LAW_BASIS_0, LAW_BASIS_1)));

        assertThat(this.client.findUsageReport())
                .extracting(LawBasisUsageReport::getClientImmigrationStatus,
                        LawBasisUsageReport::getLawCode)
                .contains(tuple("Permiso en vigor", "ES-LB-001"),
                        tuple("Permiso en vigor", "ES-LB-002"));
    }
}
