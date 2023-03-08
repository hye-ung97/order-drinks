package com.zerobase.order_drinks.model.dto;

import com.zerobase.order_drinks.model.constants.Pay;
import lombok.Data;

@Data
public class Order {
    private String item;
    private String userName;
    private String storeName;
    private Pay pay;
}
