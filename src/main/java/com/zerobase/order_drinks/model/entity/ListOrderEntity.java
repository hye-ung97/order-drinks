package com.zerobase.order_drinks.model.entity;

import com.zerobase.order_drinks.model.constants.OrderStatus;
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
    private int no;

    private String userName;

    private LocalDateTime orderDateTime;


    private String menu;

    private int price;

    private String store;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private int quantity;
    private LocalDateTime orderCompleteDateTime;
}
