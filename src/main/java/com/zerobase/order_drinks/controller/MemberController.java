package com.zerobase.order_drinks.controller;

import com.zerobase.order_drinks.model.dto.Auth;
import com.zerobase.order_drinks.model.dto.OrderBillDto;
import com.zerobase.order_drinks.service.MemberService;
import com.zerobase.order_drinks.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final OrderService orderService;

    @GetMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody Auth.SignIn signIn){
        return ResponseEntity.ok(memberService.withdraw(signIn));
    }

    @GetMapping("/wallet")
    public ResponseEntity<?> getWallet(@AuthenticationPrincipal UserDetails user){
        var result = memberService.getWallet(user.getUsername());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/charge")
    public ResponseEntity<?> charge(@RequestParam("price") int price, @AuthenticationPrincipal UserDetails user){
        var result = memberService.cardCharge(price, user.getUsername());
        return ResponseEntity.ok(result);
    }

    @GetMapping("/myOrder")
    public ResponseEntity<?> getUserOrder(@AuthenticationPrincipal UserDetails user, Pageable pageable){
        Page<OrderBillDto> result = orderService.getUserOrderList(user.getUsername(), pageable);
        return ResponseEntity.ok(result);
    }

}
