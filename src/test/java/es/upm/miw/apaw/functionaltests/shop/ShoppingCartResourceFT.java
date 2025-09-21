package es.upm.miw.apaw.functionaltests.shop;

import es.upm.miw.apaw.functionaltests.UserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ShoppingCartResourceFT {

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient.bindToServer()
                .baseUrl("http://localhost:8080")
                .build();
    }

    @Test
    void testCreate() {
        ArticleItem articleItem = ArticleItem.builder().article(
                Article.builder().barcode("84001").build()).amount(100).discount(BigDecimal.ZERO).build();
        ShoppingCart shoppingCart = ShoppingCart.builder()
                .articleItems(List.of(articleItem))
                .user(UserDto.builder().id(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0002")).build())
                .build();

        webTestClient.post()
                .uri("/shop/shopping-carts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(shoppingCart)
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/shop/shopping-carts")
                        .queryParam("price", 100.0)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(ShoppingCart.class)
                .value(carts -> assertThat(carts)
                        .extracting(ShoppingCart::getUser)
                        .extracting(UserDto::getId)
                        .contains(UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0002")));
    }

}
