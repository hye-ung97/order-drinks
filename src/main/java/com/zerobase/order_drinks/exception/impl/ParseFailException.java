package com.zerobase.order_drinks.exception.impl;

import com.zerobase.order_drinks.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class ParseFailException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "api 에서 데이터 가져오는 것을 실패하였습니다.";
    }
}
