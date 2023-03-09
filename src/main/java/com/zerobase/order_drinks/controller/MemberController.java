package com.zerobase.order_drinks.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zerobase.order_drinks.model.dto.Auth;
import com.zerobase.order_drinks.model.dto.Order;
import com.zerobase.order_drinks.model.dto.StoreData;
import com.zerobase.order_drinks.security.TokenProvider;
import com.zerobase.order_drinks.service.GoogleMapService;
import com.zerobase.order_drinks.service.MemberService;
import com.zerobase.order_drinks.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    @GetMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody Auth.SignIn signIn){
        return ResponseEntity.ok(memberService.withdraw(signIn));
    }

    @GetMapping("/wallet")
    public ResponseEntity<?> getWallet(@RequestHeader("Authorization") String token){
        String userName = this.tokenProvider.getUsername(token.replace("Bearer ", ""));
        var result = memberService.getWallet(userName);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/charge")
    public ResponseEntity<?> charge(@RequestParam("price") int price, @RequestHeader("Authorization") String token){
        String userName = this.tokenProvider.getUsername(token.replace("Bearer ", ""));
        var result = memberService.cardCharge(price, userName);
        log.info("token : " + userName);
        return ResponseEntity.ok(result);
    }

}
