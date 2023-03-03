package com.zerobase.order_drinks.member;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/order")
public class OrderController {

    @RequestMapping("/menu")
    public String orderMenu() {
        return "order/menu";
    }

    @RequestMapping("/orderedList")
    public String orderList() {
        return "order/orderedList";
    }
}
