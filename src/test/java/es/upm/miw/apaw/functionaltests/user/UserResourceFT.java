package es.upm.miw.apaw.functionaltests.user;

import feign.FeignException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = UserResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class UserResourceFT {

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EnableFeignClients(clients = UserClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private UserClient userClient;

    private UserDto user;
    private boolean created;

    @BeforeEach
    void setUp() {
        String mobile;
        do {
            mobile = Long.toString(ThreadLocalRandom.current().nextLong(600_000_000L, 700_000_000L));
        } while (!this.userClient.find(UserFindCriteria.builder().mobile(mobile).build()).isEmpty());
        this.user = UserDto.builder().mobile(mobile).firstName("Feign test")
                .familyName("Functional").email("feign@example.com")
                .address("Test address").active(true).build();
    }

    @AfterEach
    void tearDown() {
        if (this.created) {
            this.userClient.find(UserFindCriteria.builder().mobile(this.user.getMobile()).build())
                    .forEach(found -> this.userClient.delete(found.getId()));
        }
    }

    private UserDto createUser() {
        this.userClient.create(this.user);
        this.created = true;
        return this.userClient.readByMobile(this.user.getMobile());
    }

    @Test
    void testCreateAndReadByMobile() {
        UserDto actual = this.createUser();
        assertThat(actual.getId()).isNotNull();
        assertThat(actual).usingRecursiveComparison().ignoringFields("id").isEqualTo(this.user);
    }

    @Test
    void testReadById() {
        UserDto actual = this.createUser();
        assertThat(this.userClient.readById(actual.getId())).isEqualTo(actual);
    }

    @Test
    void testFindReturnsSummary() {
        UserDto actual = this.createUser();
        assertThat(this.userClient.find(UserFindCriteria.builder().mobile(this.user.getMobile()).build()))
                .singleElement().satisfies(summary -> {
                    assertThat(summary.getId()).isEqualTo(actual.getId());
                    assertThat(summary.getMobile()).isEqualTo(this.user.getMobile());
                    assertThat(summary.getFirstName()).isEqualTo(this.user.getFirstName());
                    assertThat(summary.getFamilyName()).isEqualTo(this.user.getFamilyName());
                    assertThat(summary.getEmail()).isEqualTo(this.user.getEmail());
                    assertThat(summary.getAddress()).isNull();
                    assertThat(summary.getActive()).isNull();
                });
    }

    @Test
    void testFindByActiveAndMobile() {
        this.createUser();
        assertThat(this.userClient.find(UserFindCriteria.builder()
                .mobile(this.user.getMobile()).active(true).build())).hasSize(1);
        assertThat(this.userClient.find(UserFindCriteria.builder()
                .mobile(this.user.getMobile()).active(false).build())).isEmpty();
    }

    @Test
    void testDelete() {
        UUID id = this.createUser().getId();
        this.userClient.delete(id);
        assertThatThrownBy(() -> this.userClient.readById(id)).isInstanceOf(FeignException.NotFound.class);
        assertThatThrownBy(() -> this.userClient.readByMobile(this.user.getMobile()))
                .isInstanceOf(FeignException.NotFound.class);
        assertThat(this.userClient.find(UserFindCriteria.builder().mobile(this.user.getMobile()).build())).isEmpty();
    }

    @Test
    void testReadByIdNotFound() {
        assertThatThrownBy(() -> this.userClient.readById(UUID.randomUUID()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testReadByMobileNotFound() {
        assertThatThrownBy(() -> this.userClient.readByMobile(this.user.getMobile()))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testCreateWithoutFirstName() {
        this.user.setFirstName(null);
        assertThatThrownBy(() -> this.userClient.create(this.user)).isInstanceOf(FeignException.BadRequest.class);
    }

    @Test
    void testCreateWithInvalidMobile() {
        this.user.setMobile("invalid-mobile");
        assertThatThrownBy(() -> this.userClient.create(this.user)).isInstanceOf(FeignException.BadRequest.class);
    }
}
