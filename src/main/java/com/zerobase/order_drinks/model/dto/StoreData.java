package com.zerobase.order_drinks.model.dto;

import com.zerobase.order_drinks.model.entity.StoreEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@Schema(description = "지점 DTO")
public class StoreData {
    @Schema(description = "지점명" ,example = "스타벅스 역삼대로점")
    private String storeName;
    @Schema(description = "주소", example = "대한민국 서울특별시 강남구 테헤란로 211")
    private String address;
    @Schema(description = "입력한 주소에서 지점까지의 거리", example = "0.227 km")
    private String distance;

    public StoreEntity toEntity(){
        return StoreEntity.builder()
                .storeName(this.getStoreName())
                .address(this.getAddress())
                .build();
    }
}
