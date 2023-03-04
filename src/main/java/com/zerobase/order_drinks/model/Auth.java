package com.zerobase.order_drinks.model;

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

        public MemberEntity toEntity(String uuid){
            return MemberEntity.builder()
                                    .username(this.username)
                                    .password(this.password)
                                    .roles(this.roles)
                                    .emailAuthStatus(false)
                                    .registerDate(LocalDateTime.now())
                                    .emailAuthKey(uuid)
                                .build();
        }
    }
}
