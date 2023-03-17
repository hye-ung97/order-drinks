package com.zerobase.order_drinks.model.dto;

import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import lombok.Data;

import java.util.List;

@Data
public class StoreOrderBillDto {
    private long sum;
    private List<ListOrderEntity> orderList;
}
