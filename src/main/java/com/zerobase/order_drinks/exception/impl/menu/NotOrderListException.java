package com.zerobase.order_drinks.exception.impl.menu;

import com.zerobase.order_drinks.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class NotOrderListException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "요청하신 상태의 주문 리스트는 없습니다.";
    }
}
