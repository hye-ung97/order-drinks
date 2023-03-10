package com.zerobase.order_drinks.model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class Wallet {

    private Wallet.Card card;
    private Wallet.Point point;
    private Wallet.Coupon coupon;

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

    public void setWallet(Card card, Coupon coupon, Point point){
        this.card = card;
        this.coupon = coupon;
        this.point = point;
    }
}
