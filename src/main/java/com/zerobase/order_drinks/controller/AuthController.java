package com.zerobase.order_drinks.controller;


import com.zerobase.order_drinks.exception.CustomException;
import com.zerobase.order_drinks.model.dto.Auth;
import com.zerobase.order_drinks.security.TokenProvider;
import com.zerobase.order_drinks.service.MemberService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;
    private final TokenProvider tokenProvider;

    @Operation(summary = "회원가입", description = "회원가입")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = Auth.SignUp.class))),
            @ApiResponse(responseCode = "ALREADY_EXIST_USER", description = "이미 존재하는 아이디 입니다.",
                    content = @Content(schema = @Schema(implementation = CustomException.class))),
            @ApiResponse(responseCode = "NO_EMAIL_PATTERN", description = "이메일 형식이 아닙니다.",
                    content = @Content(schema = @Schema(implementation = CustomException.class)))
    })
    @PostMapping("/signup")
    public ResponseEntity<Auth.SignUp> signup(@RequestBody Auth.SignUp request){
        memberService.register(request);
        log.info("user register -> " + request.getUsername());

        return ResponseEntity.ok(request);
    }

    @Operation(summary = "로그인", description = "로그인후 토큰 발급")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "로그인 성공",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "NOT_EXIST_USER", description = "존재하지 않는 사용자 입니다.",
                    content = @Content(schema = @Schema(implementation = CustomException.class))),
            @ApiResponse(responseCode = "PASSWORD_NOT_MATCH", description = "비밀번호가 일치하지 않습니다.",
                    content = @Content(schema = @Schema(implementation = CustomException.class))),
            @ApiResponse(responseCode = "NO_EMAIL_AUTH", description = "이메일 인증이 완료되지 않았습니다.",
                    content = @Content(schema = @Schema(implementation = CustomException.class))),
            @ApiResponse(responseCode = "WITHDRAW_USER", description = "회원탈퇴한 회원 입니다.",
                    content = @Content(schema = @Schema(implementation = CustomException.class)))
    })
    @PostMapping("/signin")
    public ResponseEntity<?> signin(@RequestBody Auth.SignIn request){

        var member = this.memberService.authenticate(request);
        var token = this.tokenProvider.generateToken(member.getUsername(), member.getRoles());

        log.info("user login -> " + request.getUsername());
        return ResponseEntity.ok(token);
    }
    @Operation(summary = "이메일 인증", description = "이메일 인증 과정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이메일 인증 성공",
                    content = @Content(schema = @Schema(implementation = String.class))),
            @ApiResponse(responseCode = "NOT_EXIST_USER", description = "존재하지 않는 사용자 입니다.",
                    content = @Content(schema = @Schema(implementation = CustomException.class)))
    })
    @GetMapping("/email-auth")
    public ResponseEntity<?> emailAuth(@RequestParam("id") String uuid){
        var member = memberService.emailAuth(uuid);
        log.info("user email Auth ok");
        return ResponseEntity.ok(member);
    }
}
