package com.zerobase.order_drinks.model.dto;

import com.zerobase.order_drinks.model.constants.OrderStatus;
import com.zerobase.order_drinks.model.constants.Pay;
import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class Order {
    private String item;
    private int quantity;
    private String storeName;
    private Pay pay;

    public ListOrderEntity toEntity(int price, String userName){
        return ListOrderEntity.builder()
                .menu(this.item)
                .store(this.storeName)
                .price(price)
                .quantity(this.quantity)
                .userName(userName)
                .orderStatus(OrderStatus.ING)
                .orderDateTime(LocalDateTime.now())
                .pay(this.getPay())
                .build();
    }
}
