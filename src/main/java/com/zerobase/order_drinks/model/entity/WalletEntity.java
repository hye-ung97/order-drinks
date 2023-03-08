package com.zerobase.order_drinks.model.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

public class WalletEntity {

    @Entity
    @Data
    public static class Card{
        @Id
        @Column(name = "CARD_ID")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        private int price;
        private LocalDateTime chargedDate;

    }

    @Entity
    @Data
    public static class Point{
        @Id
        @Column(name = "POINT_ID")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        private int count;
        private LocalDateTime updatedDate;

    }

    @Entity
    @Data
    public static class Coupon{
        @Id
        @Column(name = "COUPON_ID")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private int id;

        private int count;
        private LocalDateTime updatedDate;
    }
}
