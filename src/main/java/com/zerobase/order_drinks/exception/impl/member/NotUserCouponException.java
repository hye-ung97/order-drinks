package com.zerobase.order_drinks.exception.impl.member;

import com.zerobase.order_drinks.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class NotUserCouponException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "사용가능한 쿠폰이 없습니다.";
    }
}
