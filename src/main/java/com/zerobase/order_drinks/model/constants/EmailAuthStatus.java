package com.zerobase.order_drinks.model.constants;

public enum EmailAuthStatus {
    ING ("이메일 인증 중"),
    COMPLETE ("이메일 인증 완료");


   private String message;

   EmailAuthStatus(String message){
       this.message = message;
   }

   public String getMessage(){
       return this.message;
   }
}
