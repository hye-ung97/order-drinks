package com.zerobase.order_drinks.controller;


import com.zerobase.order_drinks.model.Auth;
import com.zerobase.order_drinks.security.TokenProvider;
import com.zerobase.order_drinks.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody Auth.SignUp request){
        var result = this.memberService.register(request);

        return ResponseEntity.ok(request);
    }

    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request){

        var member = this.memberService.authenticate(request);
        var token = this.tokenProvider.generateToken(member.getUsername(), member.getRoles());

        log.info("user login -> " + request.getUsername());
        return ResponseEntity.ok(token);
    }

    @GetMapping("/email-auth")
    public ResponseEntity<?> emailAuth(@RequestParam("id") String uuid){
        //String uuid = param;
        var member = memberService.emailAuth(uuid);

        log.info("user email Auth ok");

        return ResponseEntity.ok(member);
    }
}
