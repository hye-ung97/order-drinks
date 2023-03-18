package com.zerobase.order_drinks.model.dto;

import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
public class StoreOrderBillDto {
    private long sum;
    private Page<ListOrderEntity> orderList;
}
