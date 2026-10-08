package es.upm.miw.apaw.functionaltests.probate;

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

@SpringBootTest(classes = EstateResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class EstateResourceFT {
    private static final UUID HEIR_ID = UUID.fromString("c0c0c0c0-d0d0-e0e0-f0f0-a0a0a0a00000");
    private static final UUID USER_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = EstateClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private EstateClient client;

    @Test
    void testCreate() {
        CreationEstate creation = CreationEstate.builder()
                .fileNumber("EXP-" + UUID.randomUUID())
                .deceasedName("Feign deceased")
                .netValue(new BigDecimal("100000.00"))
                .lastWill(true)
                .heirIds(List.of(HEIR_ID))
                .userId(USER_ID)
                .build();

        Estate estate = this.client.create(creation);

        assertThat(estate.getId()).isNotNull();
        assertThat(estate.getFileNumber()).isEqualTo(creation.getFileNumber());
        assertThat(estate.getOpenedDate()).isNotNull();
        assertThat(estate.getUserSnapshot()).isNotNull();
        assertThat(estate.getUserSnapshot().getId()).isEqualTo(USER_ID);
        assertThat(estate.getHeirs()).extracting(Heir::getId).containsExactly(HEIR_ID);
    }

    @Test
    void testSearchByFileNumber() {
        EstateFindCriteria criteria = EstateFindCriteria.builder()
                .fileNumber("EXP-2025-001")
                .build();
        List<Estate> estates = this.client.search(criteria);
        assertThat(estates).extracting(Estate::getFileNumber).contains("EXP-2025-001");
    }

    @Test
    void testSearchOpened() {
        EstateFindCriteria criteria = EstateFindCriteria.builder()
                .opened(true)
                .build();
        List<Estate> estates = this.client.search(criteria);
        assertThat(estates).allSatisfy(estate -> assertThat(estate.getClosingDate()).isNull());
    }

    @Test
    void testUsageReport() {
        List<EstateUsageReport> report = this.client.usageReport();
        assertThat(report).isNotEmpty();
    }
}
