package com.zerobase.order_drinks.model.constants;

public enum OrderStatus {
    ING ("음료 제작 중"),
    COMPLETE ("음료 제작 완료");


   String message;

   OrderStatus(String message){
       this.message = message;
   }
}
