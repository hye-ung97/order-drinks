package com.zerobase.order_drinks.model.dto;

import com.zerobase.order_drinks.model.constants.Pay;
import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderComplete {
    private String item;
    private int quantity;
    private int totalPrice;
    private String storeName;
    private Pay pay;
    private LocalDateTime orderTime;

    public OrderComplete toDto (ListOrderEntity listOrder, Pay pay){
        OrderComplete orderComplete = new OrderComplete();
        orderComplete.setOrderTime(listOrder.getOrderDateTime());
        orderComplete.setPay(pay);
        orderComplete.setItem(listOrder.getMenu());
        orderComplete.setQuantity(listOrder.getQuantity());
        orderComplete.setTotalPrice(listOrder.getPrice());
        orderComplete.setStoreName(listOrder.getStore());
        return orderComplete;
    }
}
