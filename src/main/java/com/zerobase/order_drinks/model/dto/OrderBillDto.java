package com.zerobase.order_drinks.model.dto;

import com.zerobase.order_drinks.model.constants.OrderStatus;
import com.zerobase.order_drinks.model.constants.Pay;
import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class OrderBillDto {
    private String item;
    private int quantity;
    private int totalPrice;
    private String storeName;
    private Pay pay;
    private LocalDateTime orderTime;
    private OrderStatus status;

    public OrderBillDto toDto (ListOrderEntity listOrder){
        OrderBillDto orderComplete = new OrderBillDto();
        orderComplete.setOrderTime(listOrder.getOrderDateTime());
        orderComplete.setPay(listOrder.getPay());
        orderComplete.setItem(listOrder.getMenu());
        orderComplete.setQuantity(listOrder.getQuantity());
        orderComplete.setTotalPrice(listOrder.getPrice());
        orderComplete.setStoreName(listOrder.getStore());
        orderComplete.setStatus(listOrder.getOrderStatus());
        return orderComplete;
    }
}
