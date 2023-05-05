package com.zerobase.order_drinks.model.dto;

import lombok.*;

@Builder
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class StoreGroup {
    String storeName;
    long totalPrice;
}
