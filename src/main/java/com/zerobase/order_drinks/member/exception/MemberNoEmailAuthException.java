package com.zerobase.order_drinks.member.exception;

public class MemberNoEmailAuthException extends RuntimeException {
    public MemberNoEmailAuthException(){
        super("이메일 활성화 이후에 로그인을 해주세요.");
    }
}
