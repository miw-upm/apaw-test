package es.upm.miw.apaw.functionaltests.judicialcourt;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = JudicialCourtResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class JudicialCourtResourceFT {
    private static final UUID TYPE_ID = UUID.fromString("eeeeeeee-ffff-aaaa-bbbb-ccccdddd0000");
    private static final UUID LAWYER_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = {JudicialCourtClient.class, JudicialCourtTypeClient.class})
    static class ClientConfiguration {
    }

    @Autowired
    private JudicialCourtClient client;

    @Autowired
    private JudicialCourtTypeClient typeClient;

    @Test
    void testCreate() {
        JudicialCourtType type = this.typeClient.create(JudicialCourtType.builder()
                .name("Type for create " + UUID.randomUUID())
                .description("Court type")
                .code("TC" + UUID.randomUUID().toString().substring(0, 6).toUpperCase())
                .jurisdiction("Penal")
                .build());

        CreationJudicialCourt creation = CreationJudicialCourt.builder()
                .name("Juzgado de prueba " + UUID.randomUUID())
                .number(1)
                .address("Calle de la Justicia")
                .city("Madrid")
                .postalCode("28001")
                .phone("912345678")
                .email("court@test.es")
                .typeId(type.getId())
                .lawyerIds(List.of(LAWYER_ID))
                .build();

        JudicialCourt court = this.client.create(creation);

        assertThat(court.getId()).isNotNull();
        assertThat(court.getName()).isEqualTo(creation.getName());
        assertThat(court.getCity()).isEqualTo("Madrid");
        assertThat(court.getCreatedAt()).isNotNull();
        assertThat(court.getStatus()).isEqualTo(JudicialCourtStatus.ACTIVE);
        assertThat(court.getType().getId()).isEqualTo(type.getId());
        assertThat(court.getLawyers()).extracting(UserSnapshot::getId).contains(LAWYER_ID);
    }

    @Test
    void testFindByCity() {
        JudicialCourtType type = this.typeClient.create(JudicialCourtType.builder()
                .name("Type city " + UUID.randomUUID())
                .description("Court type")
                .code("TCI" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .jurisdiction("Civil")
                .build());

        this.client.create(CreationJudicialCourt.builder()
                .name("Madrid court " + UUID.randomUUID())
                .address("Calle 1")
                .city("Madrid")
                .typeId(type.getId())
                .lawyerIds(List.of(LAWYER_ID))
                .build());

        this.client.create(CreationJudicialCourt.builder()
                .name("Barcelona court " + UUID.randomUUID())
                .address("Calle 2")
                .city("Barcelona")
                .typeId(type.getId())
                .lawyerIds(List.of(LAWYER_ID))
                .build());

        List<JudicialCourt> courts = this.client.find("Madrid", null, null, null);

        assertThat(courts).extracting(JudicialCourt::getCity).allMatch(city -> city.equals("Madrid"));
    }

    @Test
    void testFindByCompleteContactInformation() {
        JudicialCourtType type = this.typeClient.create(JudicialCourtType.builder()
                .name("Type contact " + UUID.randomUUID())
                .description("Court type")
                .code("TCO" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .jurisdiction("Mercantil")
                .build());

        this.client.create(CreationJudicialCourt.builder()
                .name("Complete court " + UUID.randomUUID())
                .address("Calle A")
                .city("Madrid")
                .phone("912345678")
                .email("complete@test.es")
                .typeId(type.getId())
                .lawyerIds(List.of(LAWYER_ID))
                .build());

        this.client.create(CreationJudicialCourt.builder()
                .name("Incomplete court " + UUID.randomUUID())
                .address("Calle B")
                .city("Madrid")
                .typeId(type.getId())
                .lawyerIds(List.of(LAWYER_ID))
                .build());

        List<JudicialCourt> completeCourts = this.client.find(null, true, null, null);

        assertThat(completeCourts).allMatch(c -> c.getEmail() != null && c.getPhone() != null);
    }

    @Test
    void testFindByJurisdiction() {
        JudicialCourtType penalType = this.typeClient.create(JudicialCourtType.builder()
                .name("Juzgado Penal " + UUID.randomUUID())
                .description("Penal court")
                .code("JP" + UUID.randomUUID().toString().substring(0, 7).toUpperCase())
                .jurisdiction("Penal")
                .build());

        JudicialCourtType civilType = this.typeClient.create(JudicialCourtType.builder()
                .name("Juzgado Civil " + UUID.randomUUID())
                .description("Civil court")
                .code("JC" + UUID.randomUUID().toString().substring(0, 7).toUpperCase())
                .jurisdiction("Civil")
                .build());

        this.client.create(CreationJudicialCourt.builder()
                .name("Penal court " + UUID.randomUUID())
                .address("Addr P")
                .city("Madrid")
                .typeId(penalType.getId())
                .lawyerIds(List.of(LAWYER_ID))
                .build());

        this.client.create(CreationJudicialCourt.builder()
                .name("Civil court " + UUID.randomUUID())
                .address("Addr C")
                .city("Barcelona")
                .typeId(civilType.getId())
                .lawyerIds(List.of(LAWYER_ID))
                .build());

        List<JudicialCourt> penalCourts = this.client.find(null, null, "Penal", null);

        assertThat(penalCourts).anyMatch(c -> c.getType().getJurisdiction().equals("Penal"));
    }

    @Test
    void testFindAll() {
        JudicialCourtType type = this.typeClient.create(JudicialCourtType.builder()
                .name("Type findall " + UUID.randomUUID())
                .description("Court type")
                .code("TFA" + UUID.randomUUID().toString().substring(0, 5).toUpperCase())
                .jurisdiction("Social")
                .build());

        this.client.create(CreationJudicialCourt.builder()
                .name("Court findall " + UUID.randomUUID())
                .address("Addr")
                .city("Madrid")
                .typeId(type.getId())
                .lawyerIds(List.of(LAWYER_ID))
                .build());

        List<JudicialCourt> courts = this.client.find(null, null, null, null);

        assertThat(courts).isNotEmpty();
    }

    @Test
    void testFindRanking() {
        JudicialCourtType type = this.typeClient.create(JudicialCourtType.builder()
                .name("Type ranking " + UUID.randomUUID())
                .description("Court type")
                .code("TR" + UUID.randomUUID().toString().substring(0, 7).toUpperCase())
                .jurisdiction("Contencioso")
                .build());

        UUID lawyer1 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0001");
        UUID lawyer2 = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0002");

        this.client.create(CreationJudicialCourt.builder()
                .name("Ranking court 1 " + UUID.randomUUID())
                .address("Addr")
                .city("Madrid")
                .typeId(type.getId())
                .lawyerIds(List.of(lawyer1, lawyer2))
                .build());

        this.client.create(CreationJudicialCourt.builder()
                .name("Ranking court 2 " + UUID.randomUUID())
                .address("Addr")
                .city("Madrid")
                .typeId(type.getId())
                .lawyerIds(List.of(lawyer1))
                .build());

        List<LawyerCourtRankingReport> ranking = this.client.ranking();

        assertThat(ranking).isNotEmpty();
        assertThat(ranking).extracting(LawyerCourtRankingReport::getTotalJudicialCourts).isNotEmpty();
    }

    @Test
    void testFindRankingEmpty() {
        // Just verifies it returns empty list without error
        assertThat(this.client.ranking()).isNotNull();
    }

    @Test
    void testCreateMissingType() {
        UUID missingTypeId = UUID.randomUUID();

        assertThatThrownBy(() -> this.client.create(CreationJudicialCourt.builder()
                .name("Court " + UUID.randomUUID())
                .address("Addr")
                .city("Madrid")
                .typeId(missingTypeId)
                .lawyerIds(List.of(LAWYER_ID))
                .build()))
                .isInstanceOf(FeignException.NotFound.class);
    }
}
