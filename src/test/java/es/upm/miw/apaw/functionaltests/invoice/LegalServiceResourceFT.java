package es.upm.miw.apaw.functionaltests.invoice;

import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(
        classes = LegalServiceResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
class LegalServiceResourceFT {

    private static final UUID SERVICE_0 =
            UUID.fromString("a1234567-bbbb-cccc-dddd-eeeeffff0000");

    private static final UUID SERVICE_1 =
            UUID.fromString("a1234567-bbbb-cccc-dddd-eeeeffff0001");

    private static final UUID UNKNOWN_ID =
            UUID.fromString("00000000-0000-0000-0000-000000000000");

    @Configuration
    @EnableAutoConfiguration
    @EnableFeignClients(clients = LegalServiceClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private LegalServiceClient client;

    @Test
    void testFindById() {
        LegalService service = this.client.findById(SERVICE_0);

        assertThat(service.getId()).isEqualTo(SERVICE_0);
        assertThat(service.getName()).isEqualTo("Initial Legal Consultation");
        assertThat(service.getDescription())
                .isEqualTo("First consultation to assess the client's legal situation");
        assertThat(service.getFee()).isEqualByComparingTo("80.00");
        assertThat(service.getCategory()).isEqualTo(ServiceCategory.CONSULTING);
        assertThat(service.getLegalArea()).isEqualTo(LegalArea.CIVIL);
        assertThat(service.getRequiresAppointment()).isTrue();
    }

    @Test
    void testFindAnotherServiceById() {
        LegalService service = this.client.findById(SERVICE_1);

        assertThat(service.getId()).isEqualTo(SERVICE_1);
        assertThat(service.getName()).isEqualTo("Criminal Law Consultation");
        assertThat(service.getFee()).isEqualByComparingTo("200.00");
        assertThat(service.getLegalArea()).isEqualTo(LegalArea.CRIMINAL);
    }

    @Test
    void testFindUnknownService() {
        assertThatThrownBy(() -> this.client.findById(UNKNOWN_ID))
                .isInstanceOf(FeignException.class)
                .satisfies(exception ->
                        assertThat(((FeignException) exception).status())
                                .isEqualTo(404));
    }

}
