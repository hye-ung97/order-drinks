package com.zerobase.order_drinks.model.dto;

import com.zerobase.order_drinks.model.entity.MenuEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "메뉴 DTO")
public class Menu {

    @Schema(description = "메뉴명", example = "아메리카노")
    private String menuName;
    @Schema(description = "금액", example = "4100")
    private int price;


    public MenuEntity toEntity(){
        return MenuEntity.builder()
                .registerDateTime(LocalDateTime.now())
                .menuName(this.menuName)
                .price(this.price)
                .build();
    }

    public Menu menuDto(String menuName, int price) {
        Menu menu = new Menu();
        menu.setMenuName(menuName);
        menu.setPrice(price);
        return menu;
    }
}
