package com.zerobase.order_drinks.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.data.domain.Page;

@Data
@Schema(description = "지점 매출 및 주문서 DTO")
public class StoreOrderBillDto {
    @Schema(description = "지점 매출 합계", example = "4100")
    private long sum;
    @Schema(description = "주문 리스트")
    private Page<ListOrderDto> orderList;
}
