package com.zerobase.order_drinks.exception.impl.member;

import com.zerobase.order_drinks.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class NoEmailPatternException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "이메일 형식이 아닙니다.";
    }
}
