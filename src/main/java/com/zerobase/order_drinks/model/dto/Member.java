package com.zerobase.order_drinks.model.dto;

import com.zerobase.order_drinks.model.constants.EmailAuthStatus;
import com.zerobase.order_drinks.model.constants.MemberStatus;
import com.zerobase.order_drinks.model.entity.MemberEntity;
import com.zerobase.order_drinks.model.entity.WalletEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    private String username;
    private EmailAuthStatus emailAuthStatus;
    private LocalDateTime emailAuthDateTime;
    private List<String> roles;
    private LocalDateTime registerDateTime;
    private MemberStatus memberStatus;

    private WalletEntity.Card card;
    private WalletEntity.Point point;
    private WalletEntity.Coupon coupon;


    public void toMember(MemberEntity memberEntity) {
        this.username = memberEntity.getUsername();
        this.emailAuthStatus = memberEntity.getEmailAuthStatus();
        this.emailAuthDateTime = memberEntity.getEmailAuthDateTime();
        this.roles = memberEntity.getRoles();
        this.registerDateTime = memberEntity.getRegisterDateTime();
        this.card = memberEntity.getCard();
        this.point = memberEntity.getPoint();
        this.coupon = memberEntity.getCoupon();
        this.memberStatus = memberEntity.getMemberStatus();

    }
}
