package com.zerobase.order_drinks.exception.impl;

import com.zerobase.order_drinks.exception.AbstractException;
import org.springframework.http.HttpStatus;

public class MailFailException extends AbstractException {
    @Override
    public int getStatusCode() {
        return HttpStatus.BAD_REQUEST.value();
    }

    @Override
    public String getMessage() {
        return "메일 전송에 실패하였습니다.";
    }
}
