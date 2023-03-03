package com.zerobase.order_drinks.member.model;

import lombok.Data;
import lombok.ToString;

@Data
@ToString
public class MemberInput {
    private String userId;
    private String userName;
    private String password;
    private String phone;
}
