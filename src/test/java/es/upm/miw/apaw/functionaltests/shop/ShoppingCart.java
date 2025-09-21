package es.upm.miw.apaw.functionaltests.shop;

import es.upm.miw.apaw.functionaltests.UserDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingCart {
    private UUID id;
    private LocalDateTime creationDate;
    private List<ArticleItem> articleItems;
    private UserDto user;

    public static ShoppingCart ofIdUser(ShoppingCart shoppingCart) {
        ShoppingCart shoppingCartDto = new ShoppingCart();
        shoppingCartDto.setId(shoppingCart.getId());
        shoppingCartDto.setUser(shoppingCart.getUser());
        return shoppingCartDto;
    }

}
