package com.zerobase.order_drinks.model.constants;

public class MailText {
    public String subject = "Welcome!!";
    public String text = "<p>회원가입을 축하드립니다. 아래 링크를 클릭하여 회원가입을 완료 하세요</p> " +
                        "<div> <a href = 'http://localhost:8080/auth/email-auth?id=";
    public String textEnd = "'> 링크</a> </div>";
}
