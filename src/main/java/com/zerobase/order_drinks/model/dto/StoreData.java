package com.zerobase.order_drinks.model.dto;

import com.zerobase.order_drinks.model.entity.StoreEntity;
import lombok.Data;

@Data
public class StoreData {
    private String storeName;
    private String address;
    private String distance;

    public StoreEntity toEntity(){
        return StoreEntity.builder()
                .storeName(this.getStoreName())
                .address(this.getAddress())
                .build();
    }
}
