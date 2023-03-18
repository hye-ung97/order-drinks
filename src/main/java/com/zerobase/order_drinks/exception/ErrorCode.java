package com.zerobase.order_drinks.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    INTERNAL_SERVER_ERROR("내부 서버 오류가 발생하였습니다."),
    INVALID_REQUEST("잘못된 요청입니다."),
    ALREADY_EXIST_USER("이미 존재하는 사용자명입니다."),
    LOW_CARD_PRICE("잔액이 부족합니다."),
    NO_EMAIL_AUTH("이메일 인증이 완료되지 않았습니다."),
    NO_EMAIL_PATTERN("이메일 형식이 아닙니다."),
    UNAVAILABLE_COUPON("사용가능한 쿠폰이 없습니다."),
    NOT_EXIST_USER("존재하지 않는 ID 입니다."),
    PASSWORD_NOT_MATCH("비밀번호가 일치하지 않습니다."),
    WITHDRAW_USER("탈퇴한 회원입니다."),


    EXIST_MENU("이미 존재하는 음료입니다."),
    NOT_EXIST_MENU("존재하지 않는 음료입니다."),
    NOT_EXIST_MENU_LIST("메뉴 리스트가 없습니다."),
    NOT_EXIST_ORDER_LIST("요청하신 주문 리스트는 없습니다."),

    ALREADY_FINISHED_DRINK("이미 제작이 완료된 주문입니다."),
    MAIL_FAIL("메일 전송에 실패하였습니다."),
    NOT_FOUND_STORE_DATA("해당 지점 정보를 찾을 수 없습니다."),
    NOT_EXIST_STORE_SALES_DATA("요청하신 지점별 매출 리스트가 없습니다.")
    ;

    private String description;

}
