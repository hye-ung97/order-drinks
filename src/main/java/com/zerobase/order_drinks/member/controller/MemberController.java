package com.zerobase.order_drinks.member.controller;

import com.zerobase.order_drinks.member.model.MemberInput;
import com.zerobase.order_drinks.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


@RequiredArgsConstructor
@Controller
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    @RequestMapping("/login")
    public String login(){
        return "member/login";
    }

    @GetMapping("/register")
    public String register(){

        return "member/register";
    }

    @PostMapping("/register")
    public String registerSubmit(Model model, HttpServletRequest request, HttpServletResponse response, MemberInput parameter){

        boolean result = memberService.register(parameter);

        model.addAttribute("result", result);

        return "member/register-complete";
    }

    @GetMapping("/email-auth")
    public String emailAuth(Model model, HttpServletRequest request){
        String uuid = request.getParameter("id");

        boolean result = memberService.emailAuth(uuid);
        model.addAttribute("result", result);

        return "member/email-auth";
    }

    @GetMapping("/info")
    public String info(){
        return "/member/info";
    }

}
