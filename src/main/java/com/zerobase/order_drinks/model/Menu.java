package com.zerobase.order_drinks.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Menu {

    private String menuName;
    private int price;

    public MenuEntity toEntity(){
        return MenuEntity.builder()
                .registerDate(LocalDateTime.now())
                .menuName(this.menuName)
                .price(this.price)
                .build();
    }
}
