package es.upm.miw.apaw.functionaltests.user;

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

@SpringBootTest(classes = UserResourceFT.ClientConfiguration.class,
        webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class UserResourceFT {
    private static final UUID CLIENT_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000");
    private static final String CLIENT_MOBILE = "600000100";
    private static final UUID UNKNOWN_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff9999");
    private static final String NEW_MOBILE = "699999999";

    @Configuration(proxyBeanMethods = false)
    @EnableAutoConfiguration
    @EnableFeignClients(clients = UserClient.class)
    static class ClientConfiguration {
    }

    @Autowired
    private UserClient userClient;

    @Test
    void testCreateAndDelete() {
        UserDto user = UserDto.builder().mobile(NEW_MOBILE).firstName("Feign test").build();
        this.userClient.create(user);

        UserDto actual = this.userClient.readByMobile(NEW_MOBILE);
        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getMobile()).isEqualTo(NEW_MOBILE);
        assertThat(actual.getFirstName()).isEqualTo("Feign test");

        this.userClient.delete(actual.getId());
        assertThatThrownBy(() -> this.userClient.readByMobile(NEW_MOBILE))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testReadById() {
        UserDto user = this.userClient.readById(CLIENT_ID);
        assertThat(user.getId()).isEqualTo(CLIENT_ID);
        assertThat(user.getMobile()).isEqualTo(CLIENT_MOBILE);
        assertThat(user.getFirstName()).isEqualTo("cliente0");
    }

    @Test
    void testReadByMobile() {
        UserDto user = this.userClient.readByMobile(CLIENT_MOBILE);
        assertThat(user.getId()).isEqualTo(CLIENT_ID);
        assertThat(user.getMobile()).isEqualTo(CLIENT_MOBILE);
        assertThat(user.getFirstName()).isEqualTo("cliente0");
    }

    @Test
    void testFindReturnsSummary() {
        UserDto summary = UserDto.builder().id(CLIENT_ID).mobile(CLIENT_MOBILE)
                .firstName("cliente0").familyName("García López").email("cliente0@example.com").build();
        assertThat(this.userClient.find(UserFindCriteria.builder().mobile(CLIENT_MOBILE).build()))
                .containsExactly(summary);
    }

    @Test
    void testFindAll() {
        assertThat(this.userClient.find(new UserFindCriteria()))
                .extracting(UserDto::getMobile).contains(CLIENT_MOBILE, "600000109", "6");
    }

    @Test
    void testFindByActiveAndMobile() {
        assertThat(this.userClient.find(UserFindCriteria.builder().mobile(CLIENT_MOBILE).active(true).build()))
                .extracting(UserDto::getId).containsExactly(CLIENT_ID);
        assertThat(this.userClient.find(UserFindCriteria.builder().mobile(CLIENT_MOBILE).active(false).build()))
                .isEmpty();
    }

    @Test
    void testFindByBillable() {
        assertThat(this.userClient.find(UserFindCriteria.builder().mobile(CLIENT_MOBILE).billable(true).build()))
                .extracting(UserDto::getId).containsExactly(CLIENT_ID);
        assertThat(this.userClient.find(UserFindCriteria.builder().mobile("600000106").billable(true).build()))
                .isEmpty();
        assertThat(this.userClient.find(UserFindCriteria.builder().mobile("600000106").billable(false).build()))
                .extracting(UserDto::getFirstName).containsExactly("Cliente6");
    }

    @Test
    void testCreateWithDuplicateMobile() {
        UserDto user = UserDto.builder().mobile(CLIENT_MOBILE).firstName("Duplicado").build();
        assertThatThrownBy(() -> this.userClient.create(user)).isInstanceOf(FeignException.Conflict.class);
    }

    @Test
    void testReadByIdNotFound() {
        assertThatThrownBy(() -> this.userClient.readById(UNKNOWN_ID))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @Test
    void testReadByMobileNotFound() {
        assertThatThrownBy(() -> this.userClient.readByMobile("699999998"))
                .isInstanceOf(FeignException.NotFound.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" "})
    void testCreateWithInvalidFirstName(String firstName) {
        UserDto user = UserDto.builder().mobile(NEW_MOBILE).firstName(firstName).build();
        assertThatThrownBy(() -> this.userClient.create(user)).isInstanceOf(FeignException.BadRequest.class);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "invalid-mobile", "60000010"})
    void testCreateWithInvalidMobile(String mobile) {
        UserDto user = UserDto.builder().mobile(mobile).firstName("Feign test").build();
        assertThatThrownBy(() -> this.userClient.create(user)).isInstanceOf(FeignException.BadRequest.class);
    }
}
