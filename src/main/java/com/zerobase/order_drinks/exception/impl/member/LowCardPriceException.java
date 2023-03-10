package com.zerobase.order_drinks.exception.impl.member;

import com.zerobase.order_drinks.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class LowCardPriceException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "잔액이 부족합니다.";
    }
}
