package com.zerobase.order_drinks.model.dto;

import com.zerobase.order_drinks.model.entity.MemberEntity;
import com.zerobase.order_drinks.model.constants.EmailAuthStatus;
import com.zerobase.order_drinks.model.constants.MemberStatus;
import com.zerobase.order_drinks.model.entity.Wallet;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class Auth {
    @Data
    public static class SignIn{
        private String username;
        private String password;
    }

    @Data
    public static class SignUp{
        private String username;
        private String password;
        private List<String> roles;

        public MemberEntity toEntity(String uuid, Wallet.Card card, Wallet.Coupon coupon, Wallet.Point point){
            return MemberEntity.builder()
                                    .username(this.username)
                                    .password(this.password)
                                    .roles(this.roles)
                                    .emailAuthStatus(EmailAuthStatus.ING)
                                    .registerDateTime(LocalDateTime.now())
                                    .emailAuthKey(uuid)
                                    .memberStatus(MemberStatus.REQ)
                                    .card(card)
                                    .coupon(coupon)
                                    .point(point)
                                .build();
        }
    }
}
