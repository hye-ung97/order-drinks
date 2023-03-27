package com.zerobase.order_drinks.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "재고관리 DTO")
public class MenuInventory {

    @Schema(description = "메뉴명", example = "아메리카노")
    private String menuName;
    @Schema(description = "수량", example = "1")
    private int quantity;
}
