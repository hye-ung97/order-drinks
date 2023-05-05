package com.zerobase.order_drinks.model.dto;

import com.zerobase.order_drinks.model.constants.OrderStatus;
import com.zerobase.order_drinks.model.constants.Pay;
import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ListOrderDto {

    @Schema(description = "주문 번호", example = "1")
    private int no;

    @Schema(description = "주문자", example = "abc@naver.com")
    private String userName;

    @Schema(description = "주문 시간")
    private LocalDateTime orderDateTime;

    @Schema(description = "주문 메뉴명", example = "아메리카노")
    private String menu;

    @Schema(description = "주문 금액", example = "4100")
    private int price;
    @Schema(description = "주문 지점", example = "스타벅스 역삼대로점")
    private String store;

    @Schema(description = "주문 상태", example = "ING")
    private OrderStatus orderStatus;
    @Schema(description = "주문 수량", example = "1")
    private int quantity;
    @Schema(description = "음료 제작 완료 시간")
    private LocalDateTime orderCompleteDateTime;
    @Schema(description = "결제 수단", example = "CARD")
    private Pay pay;

    public ListOrderDto toDto(ListOrderEntity orderEntity){
        return ListOrderDto.builder()
                .no(orderEntity.getNo())
                .userName(orderEntity.getUserName().getUsername())
                .menu(orderEntity.getMenu())
                .store(orderEntity.getStore().getStoreName())
                .price(orderEntity.getPrice())
                .pay(orderEntity.getPay())
                .orderDateTime(orderEntity.getOrderDateTime())
                .orderCompleteDateTime(orderEntity.getOrderCompleteDateTime())
                .quantity(orderEntity.getQuantity())
                .orderStatus(orderEntity.getOrderStatus())
                .build();
    }
}
