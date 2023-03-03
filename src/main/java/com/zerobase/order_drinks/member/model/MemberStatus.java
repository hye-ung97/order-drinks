package com.zerobase.order_drinks.member.model;

public enum MemberStatus {
    REQ ("이메일 인증 요청"),
    ING ("회원 활동 가능"),
    WITHDRAW ("회원 탈퇴");

    String message;
    MemberStatus(String message) {
        this.message = message;
    }
}
