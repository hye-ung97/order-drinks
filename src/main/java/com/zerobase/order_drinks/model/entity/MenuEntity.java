package com.zerobase.order_drinks.model.entity;

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
    private int menuNo;

    @Column(name = "menuName", unique=true)
    private String menuName;

    private int price;

    private LocalDateTime registerDateTime;

}
