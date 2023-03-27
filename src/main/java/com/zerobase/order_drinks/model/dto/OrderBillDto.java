package com.zerobase.order_drinks.model.dto;

import com.zerobase.order_drinks.model.constants.OrderStatus;
import com.zerobase.order_drinks.model.constants.Pay;
import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "주문 완료 DTO")
public class OrderBillDto {
    @Schema(description = "주문한 메뉴", example = "아메리카노")
    private String item;
    @Schema(description = "주문 수량", example = "1")
    private int quantity;
    @Schema(description = "주문 금액", example = "4100")
    private int totalPrice;
    @Schema(description = "주문한 지점", example = "스타벅스 역삼대로점")
    private String storeName;
    @Schema(description = "결제 수단", example = "CARD")
    private Pay pay;
    @Schema(description = "주문 시간")
    private LocalDateTime orderTime;
    @Schema(description = "주문 상태", example = "ING")
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
