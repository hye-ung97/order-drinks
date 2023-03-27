package com.zerobase.order_drinks.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "MENU")
public class MenuEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "메뉴 번호", example = "1")
    private int menuNo;

    @Column(name = "menuName", unique=true)
    @Schema(description = "메뉴 이름", example = "아메리카노")
    private String menuName;
    @Schema(description = "금액", example = "4100")
    private int price;
    @Schema(description = "메뉴 등록 시간")
    private LocalDateTime registerDateTime;
    @Schema(description = "메뉴 업데이트 시간")
    private LocalDateTime updateDateTime;
    @Schema(description = "재고 수량", example = "10")
    private int quantity;
}
