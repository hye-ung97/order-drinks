package com.zerobase.order_drinks.model.dto;

import com.zerobase.order_drinks.model.constants.OrderStatus;
import com.zerobase.order_drinks.model.constants.Pay;
import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "주문서")
public class Order {
    @Schema(description = "주문 메뉴", example = "아메리카노")
    private String item;
    @Schema(description = "주문 수량", example = "1")
    private int quantity;
    @Schema(description = "주문 지점", example = "스타벅스 노량진점")
    private String storeName;
    @Schema(description = "결제 수단", example = "CARD")
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
