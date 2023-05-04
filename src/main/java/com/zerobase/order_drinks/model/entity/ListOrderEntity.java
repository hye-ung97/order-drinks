package com.zerobase.order_drinks.model.entity;

import com.zerobase.order_drinks.model.constants.OrderStatus;
import com.zerobase.order_drinks.model.constants.Pay;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity(name = "LIST_ORDER")
public class ListOrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "주문 번호", example = "1")
    private int no;

    @Schema(description = "주문자", example = "abc@naver.com")
    @ManyToOne
    @JoinColumn(name = "user_name")
    private MemberEntity userName;

    @Schema(description = "주문 시간")
    private LocalDateTime orderDateTime;

    @Schema(description = "주문 메뉴명", example = "아메리카노")
    private String menu;

    @Schema(description = "주문 금액", example = "4100")
    private int price;
    @Schema(description = "주문 지점", example = "스타벅스 역삼대로점")
    @ManyToOne
    @JoinColumn(name = "store")
    private StoreEntity store;

    @Enumerated(EnumType.STRING)
    @Schema(description = "주문 상태", example = "ING")
    private OrderStatus orderStatus;
    @Schema(description = "주문 수량", example = "1")
    private int quantity;
    @Schema(description = "음료 제작 완료 시간")
    private LocalDateTime orderCompleteDateTime;
    @Schema(description = "결제 수단", example = "CARD")
    @Enumerated(EnumType.STRING)
    private Pay pay;
}
