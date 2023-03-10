package com.zerobase.order_drinks.model.constants;

public enum MemberStatus {
    REQ ("이메일 인증 요청 중"),
    ING ("활동 가능"),
    WITHDRAW ("탈퇴한 회원");


   String message;

   MemberStatus(String message){
       this.message = message;
   }
}
