package com.zerobase.order_drinks.member.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Entity(name = "MEMBER")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userNo;

    @Column(name = "userId", unique=true)
    private String userId; // email
    private String userName;
    private String phone;
    private String password;
    private LocalDateTime regDt; // 회원가입일
    private LocalDateTime udtDt;//회원정보 수정일

    private boolean emailAuthYn; // 이메일 인증 여부
    private LocalDateTime emailAuthDt; // 이메일 인증한 날짜
    private String emailAuthKey;

    private String resetPasswordKey;
    private LocalDateTime resetPasswordLimitDt;

    private boolean adminYn;

    private String userStatus;//이용가능한상태, 정지상태

    private LocalDateTime lastLoginDt;//마지막 로그인 일자

}
