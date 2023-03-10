package com.zerobase.order_drinks.controller;

import com.zerobase.order_drinks.model.dto.Auth;
import com.zerobase.order_drinks.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody Auth.SignIn signIn){
        return ResponseEntity.ok(memberService.withdraw(signIn));
    }

    @GetMapping("/wallet")
    public ResponseEntity<?> getWallet(HttpServletRequest request){
        var result = memberService.getWallet(request.getUserPrincipal().getName());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/charge")
    public ResponseEntity<?> charge(@RequestParam("price") int price, HttpServletRequest request){
        var result = memberService.cardCharge(price, request.getUserPrincipal().getName());
        return ResponseEntity.ok(result);
    }

}
