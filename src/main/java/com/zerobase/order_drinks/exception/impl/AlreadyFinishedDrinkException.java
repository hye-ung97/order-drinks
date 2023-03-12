package com.zerobase.order_drinks.exception.impl;

import com.zerobase.order_drinks.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class AlreadyFinishedDrinkException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "이미 제작이 완료된 주문입니다.";
    }
}
