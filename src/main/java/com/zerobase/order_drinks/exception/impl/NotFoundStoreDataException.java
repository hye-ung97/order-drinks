package com.zerobase.order_drinks.exception.impl;

import com.zerobase.order_drinks.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class NotFoundStoreDataException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "해당 지점 정보를 찾을 수 없습니다. 다른 지점으로 주문 부탁 드립니다.";
    }
}
