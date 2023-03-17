package com.zerobase.order_drinks.exception;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ErrorResponse {
    private ErrorCode code;
    private String message;
}
