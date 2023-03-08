package com.zerobase.order_drinks.controller;

import com.zerobase.order_drinks.model.dto.Auth;
import com.zerobase.order_drinks.model.dto.Order;
import com.zerobase.order_drinks.model.dto.StoreData;
import com.zerobase.order_drinks.service.MemberService;
import com.zerobase.order_drinks.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;

@Slf4j
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final OrderService orderService;
    private final MemberService memberService;



    @GetMapping("/find-store")
    public ResponseEntity<?> findLocation(@RequestParam("address") String address){
        ArrayList<StoreData> storeData = orderService.getLocationData(address);
        return ResponseEntity.ok(storeData);
    }

    @PostMapping("/order") // 음료 주문
    public ResponseEntity<?> order(@RequestBody Order order){


        return null;
    }

    @GetMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody Auth.SignIn signIn){
        return ResponseEntity.ok(memberService.withdraw(signIn));
    }

    @GetMapping("/wallet")
    public ResponseEntity<?> getWallet(@RequestBody Auth.SignIn signIn){
        return null;
    }


}
