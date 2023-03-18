package com.zerobase.order_drinks.model.dto;

import com.zerobase.order_drinks.model.entity.MenuEntity;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Menu {

    private String menuName;
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
