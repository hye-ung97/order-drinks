package com.zerobase.order_drinks.model.constants;

public enum EmailAuthStatus {
    ING ("이메일 인증 중"),
    COMPLETE ("이메일 인증 완료");


   String message;

   EmailAuthStatus(String message){
       this.message = message;
   }
}
