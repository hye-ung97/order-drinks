package com.zerobase.order_drinks.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "STORE")
public class StoreEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int storeId;

    @Column(name = "storeName", unique = true)
    private String storeName;
    private String address;

}
