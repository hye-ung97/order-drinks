package com.zerobase.order_drinks.model.dto;

import com.zerobase.order_drinks.model.entity.MemberEntity;
import com.zerobase.order_drinks.model.constants.EmailAuthStatus;
import com.zerobase.order_drinks.model.constants.MemberStatus;
import com.zerobase.order_drinks.model.entity.Wallet;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

public class Auth {
    @Data
    @Schema(description = "로그인")
    public static class SignIn{
        @Schema(description = "id (email format)", example = "abc@naver.com")
        private String username;
        @Schema(description = "비밀번호")
        private String password;
    }

    @Data
    @Schema(description = "회원가입")
    public static class SignUp{
        @Schema(description = "id (email format)", example = "abc@naver.com")
        private String username;
        @Schema(description = "비밀번호")
        private String password;
        @Schema(description = "권한", example = "[\"ROLE_USER\"]")
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
