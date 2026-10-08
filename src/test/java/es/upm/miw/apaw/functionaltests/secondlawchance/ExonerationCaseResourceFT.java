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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
        classes = ExonerationCaseResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
public class ExonerationCaseResourceFT {
    private static final String DEBT_PREFIX = "99999999-aaaa-bbbb-cccc-ddddeeee";
    private static final UUID DEBT_ID_1 = UUID.fromString(DEBT_PREFIX + "0001");
    private static final UUID DEBT_ID_4 = UUID.fromString(DEBT_PREFIX + "0004");
    private static final String CASE_PREFIX = "99999999-aaaa-bbbb-cccc-ffff0000";
    private static final UUID CASE_ID_0 = UUID.fromString(CASE_PREFIX + "0000");
    private static final UUID CASE_ID_1 = UUID.fromString(CASE_PREFIX + "0001");
    private static final UUID USER_ID_0 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");
    private static final UUID UNKNOWN_ID = UUID.fromString("99999999-aaaa-bbbb-cccc-999999999999");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = ExonerationCaseClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private ExonerationCaseClient client;

    private CreationExonerationCase newCreation(String lawyer) {
        return CreationExonerationCase.builder()
                .caseNumber("FT-CASE-" + UUID.randomUUID())
                .lawyer(lawyer)
                .debtIds(List.of(DEBT_ID_1, DEBT_ID_4))
                .userId(USER_ID_0)
                .build();
    }

    private String newLawyer() {
        return "Abogado FT " + UUID.randomUUID();
    }

    @Test
    void testCreate() {
        String lawyer = this.newLawyer();
        CreationExonerationCase creation = this.newCreation(lawyer);

        ExonerationCase created = this.client.create(creation);

        assertThat(created.getId()).isNotNull();
        assertThat(created.getCaseNumber()).isEqualTo(creation.getCaseNumber());
        assertThat(created.getFilingDate()).isEqualTo(LocalDate.now());
        assertThat(created.getResolutionDate()).isNull();
        assertThat(created.getLawyer()).isEqualTo(lawyer);
        assertThat(created.getDebts())
                .extracting(Debt::getId)
                .containsExactlyInAnyOrder(DEBT_ID_1, DEBT_ID_4);
        assertThat(created.getUserSnapshot().getId()).isEqualTo(USER_ID_0);
        assertThat(created.getUserSnapshot().getMobile()).isEqualTo("600000100");
        assertThat(created.getUserSnapshot().getFirstName()).isEqualTo("cliente0");
    }

    @Test
    void testCreateWithoutLawyerAndWithResolutionDate() {
        CreationExonerationCase creation = this.newCreation(null);
        creation.setResolutionDate(LocalDate.now().plusDays(30));

        ExonerationCase created = this.client.create(creation);

        assertThat(created.getLawyer()).isNull();
        assertThat(created.getResolutionDate()).isEqualTo(LocalDate.now().plusDays(30));
    }

    @Test
    void testCreateRepeatedCaseNumber() {
        CreationExonerationCase creation = this.newCreation(this.newLawyer());
        creation.setCaseNumber("SLC-2025-0001");

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testCreatePastResolutionDate() {
        CreationExonerationCase creation = this.newCreation(this.newLawyer());
        creation.setResolutionDate(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateRepeatedDebt() {
        CreationExonerationCase creation = this.newCreation(this.newLawyer());
        creation.setDebtIds(List.of(DEBT_ID_1, DEBT_ID_1));

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateEmptyDebts() {
        CreationExonerationCase creation = this.newCreation(this.newLawyer());
        creation.setDebtIds(List.of());

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithoutUser() {
        CreationExonerationCase creation = this.newCreation(this.newLawyer());
        creation.setUserId(null);

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateInvalidCaseNumber(String caseNumber) {
        CreationExonerationCase creation = this.newCreation(this.newLawyer());
        creation.setCaseNumber(caseNumber);

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateBlankLawyer() {
        CreationExonerationCase creation = this.newCreation(" ");

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateUnknownDebt() {
        CreationExonerationCase creation = this.newCreation(this.newLawyer());
        creation.setDebtIds(List.of(DEBT_ID_1, UNKNOWN_ID));

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateUnknownUser() {
        CreationExonerationCase creation = this.newCreation(this.newLawyer());
        creation.setUserId(UNKNOWN_ID);

        assertThatThrownBy(() -> this.client.create(creation))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testFindAll() {
        List<ExonerationCase> cases = this.client.find(new ExonerationCaseFindCriteria());

        assertThat(cases)
                .extracting(ExonerationCase::getId)
                .contains(CASE_ID_0, CASE_ID_1);
    }

    @Test
    void testFindReturnsSummary() {
        ExonerationCase exonerationCase = this.client.find(new ExonerationCaseFindCriteria()).stream()
                .filter(item -> CASE_ID_0.equals(item.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(exonerationCase.getCaseNumber()).isEqualTo("SLC-2025-0001");
        assertThat(exonerationCase.getFilingDate()).isEqualTo(LocalDate.of(2025, 2, 10));
        assertThat(exonerationCase.getLawyer()).isEqualTo("Elena Martínez Ruiz");
        assertThat(exonerationCase.getDebts()).isNullOrEmpty();
        assertThat(exonerationCase.getUserSnapshot().getId()).isEqualTo(USER_ID_0);
        assertThat(exonerationCase.getUserSnapshot().getMobile()).isEqualTo("600000100");
        assertThat(exonerationCase.getUserSnapshot().getFirstName()).isEqualTo("cliente0");
    }

    @Test
    void testFindByLawyerIgnoringCase() {
        assertThat(this.client.find(ExonerationCaseFindCriteria.builder()
                .lawyer("elena martínez ruiz")
                .build()))
                .extracting(ExonerationCase::getId)
                .containsExactly(CASE_ID_0);
    }

    @Test
    void testFindByLawyerIsExactMatch() {
        assertThat(this.client.find(ExonerationCaseFindCriteria.builder()
                .lawyer("Elena")
                .build()))
                .isEmpty();
    }

    @Test
    void testFindByOpened() {
        assertThat(this.client.find(ExonerationCaseFindCriteria.builder().opened(true).build()))
                .extracting(ExonerationCase::getId)
                .contains(CASE_ID_0)
                .doesNotContain(CASE_ID_1);
        assertThat(this.client.find(ExonerationCaseFindCriteria.builder().opened(false).build()))
                .extracting(ExonerationCase::getId)
                .contains(CASE_ID_1)
                .doesNotContain(CASE_ID_0);
    }

    @Test
    void testFindByCreditorType() {
        assertThat(this.client.find(ExonerationCaseFindCriteria.builder()
                .creditorType(CreditorType.PUBLIC)
                .build()))
                .extracting(ExonerationCase::getId)
                .contains(CASE_ID_0, CASE_ID_1);
        assertThat(this.client.find(ExonerationCaseFindCriteria.builder()
                .creditorType(CreditorType.PRIVATE)
                .build()))
                .extracting(ExonerationCase::getId)
                .contains(CASE_ID_0, CASE_ID_1);
    }

    @Test
    void testFindByCreditorTypeWithoutDuplicates() {
        String lawyer = this.newLawyer();
        CreationExonerationCase creation = this.newCreation(lawyer);
        creation.setDebtIds(List.of(DEBT_ID_1, DEBT_ID_4));
        ExonerationCase created = this.client.create(creation);

        assertThat(this.client.find(ExonerationCaseFindCriteria.builder()
                .lawyer(lawyer)
                .creditorType(CreditorType.PUBLIC)
                .build()))
                .extracting(ExonerationCase::getId)
                .containsExactly(created.getId());
    }

    @Test
    void testFindByUserMobile() {
        assertThat(this.client.find(ExonerationCaseFindCriteria.builder()
                .userMobile("600000101")
                .build()))
                .extracting(ExonerationCase::getId)
                .contains(CASE_ID_1)
                .doesNotContain(CASE_ID_0);
    }

    @Test
    void testFindByUnknownUserMobile() {
        assertThat(this.client.find(ExonerationCaseFindCriteria.builder()
                .userMobile("699999999")
                .build()))
                .isEmpty();
    }

    @Test
    void testFindCombinedCriteria() {
        String lawyer = this.newLawyer();
        ExonerationCase created = this.client.create(this.newCreation(lawyer));

        assertThat(this.client.find(ExonerationCaseFindCriteria.builder()
                .lawyer(lawyer)
                .opened(true)
                .creditorType(CreditorType.PUBLIC)
                .userMobile("600000100")
                .build()))
                .extracting(ExonerationCase::getId)
                .containsExactly(created.getId());
        assertThat(this.client.find(ExonerationCaseFindCriteria.builder()
                .lawyer(lawyer)
                .opened(false)
                .build()))
                .isEmpty();
    }
}
