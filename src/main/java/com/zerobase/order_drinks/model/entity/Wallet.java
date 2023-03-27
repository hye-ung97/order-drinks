package com.zerobase.order_drinks.model.entity;

import io.swagger.v3.oas.annotations.media.Schema;
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
        @Schema(description = "카드 id", example = "1")
        private int id;
        @Schema(description = "카드 금액", example = "10000")
        private int price;
        @Schema(description = "충전 일자")
        private LocalDateTime chargedDate;

    }

    @Entity
    @Data
    public static class Point{
        @Id
        @Column(name = "POINT_ID")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Schema(description = "포인트 id", example = "1")
        private int id;
        @Schema(description = "포인트 갯수", example = "1")
        private int count;
        @Schema(description = "포인트 적립 일자")
        private LocalDateTime updatedDate;

    }

    @Entity
    @Data
    public static class Coupon{
        @Id
        @Column(name = "COUPON_ID")
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        @Schema(description = "쿠폰 id", example = "1")
        private int id;
        @Schema(description = "쿠폰 수", example = "1")
        private int count;
        @Schema(description = "쿠폰 적립 일자")
        private LocalDateTime updatedDate;
    }

    public void setWallet(Card card, Coupon coupon, Point point){
        this.card = card;
        this.coupon = coupon;
        this.point = point;
    }
}
