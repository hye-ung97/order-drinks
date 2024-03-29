package com.zerobase.order_drinks.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Notification {

    private String receiver; //알림을 받는 유저의 정보

    private String content; //알람의 내용

    private String url; //해당 알림 클릭시 이동할 mapping url

    private Boolean isRead; //알림 열람에 대한 여부
}