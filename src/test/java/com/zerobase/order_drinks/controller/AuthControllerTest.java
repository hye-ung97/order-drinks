package com.zerobase.order_drinks.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.order_drinks.exception.CustomException;
import com.zerobase.order_drinks.model.constants.EmailAuthStatus;
import com.zerobase.order_drinks.model.constants.MemberStatus;
import com.zerobase.order_drinks.model.dto.Auth;
import com.zerobase.order_drinks.model.dto.Member;
import com.zerobase.order_drinks.model.entity.MemberEntity;
import com.zerobase.order_drinks.repository.MemberRepository;
import com.zerobase.order_drinks.security.SecurityConfiguration;
import com.zerobase.order_drinks.security.TokenProvider;
import com.zerobase.order_drinks.service.MemberService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Spy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static com.zerobase.order_drinks.exception.ErrorCode.ALREADY_EXIST_USER;
import static com.zerobase.order_drinks.exception.ErrorCode.NOT_EXIST_USER;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AuthController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfiguration.class)}
)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MemberService memberService;
    @MockBean
    private MemberRepository memberRepository;
    @MockBean
    private TokenProvider tokenProvider;
    @Value("{$spring.jwt.secret}")
    private String secretKey;
    @Autowired
    private ObjectMapper objectMapper;
    @Spy
    private PasswordEncoder passwordEncoder = new MockPasswordEncoder();

    private class MockPasswordEncoder implements PasswordEncoder {
        @Override
        public String encode(CharSequence rawPassword) {
            return new BCryptPasswordEncoder().encode(rawPassword);
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return new BCryptPasswordEncoder().matches(rawPassword, encodedPassword);
        }
    }
    @Test
    @WithMockUser(username="user",roles={"USER"})
    @DisplayName("register - fail")
    void signupFail() throws Exception {
        //given
        Auth.SignUp signUp = new Auth.SignUp();
        signUp.setUsername("user@naver.com");
        signUp.setPassword("123");
        given(memberService.register(any())).willThrow(new CustomException(ALREADY_EXIST_USER));

        //when
        //then
        mockMvc.perform(post("/auth/signup").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(objectMapper.writeValueAsString(signUp)))
                .andExpect(jsonPath("$.code").value("ALREADY_EXIST_USER"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="user",roles={"USER"})
    @DisplayName("register - success")
    void signupSuccess() throws Exception {
        //given
        Auth.SignUp signUp = new Auth.SignUp();
        signUp.setUsername("user@naver.com");
        signUp.setPassword("123");
        given(memberService.register(any())).willReturn(MemberEntity.builder()
                        .memberStatus(MemberStatus.REQ)
                        .username(signUp.getUsername())
                        .emailAuthKey("uuid")
                        .registerDateTime(LocalDateTime.now())
                .build());

        //when
        //then
        mockMvc.perform(post("/auth/signup").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(objectMapper.writeValueAsString(signUp)))
                .andExpect(jsonPath("$.username").value("user@naver.com"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="user",roles={"USER"})
    @DisplayName("login - fail")
    void signinFail() throws Exception {
        //given
        Auth.SignIn login = new Auth.SignIn();
        login.setUsername("user@naver.com");
        login.setPassword("123");
        given(memberService.authenticate(any())).willThrow(new CustomException(NOT_EXIST_USER));

        //when
        //then
        mockMvc.perform(post("/auth/signin").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(jsonPath("$.code").value("NOT_EXIST_USER"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="user@naver.com",roles={"USER"})
    @DisplayName("이메일 인증 - 실패")
    void emailAuthFail() throws Exception {
        //given
        Member member = new Member();
        member.setEmailAuthStatus(EmailAuthStatus.COMPLETE);
        member.setMemberStatus(MemberStatus.ING);
        member.setEmailAuthDateTime(LocalDateTime.now());
        member.setUsername("user@naver.com");

        given(memberService.emailAuth(anyString())).willThrow(new CustomException(NOT_EXIST_USER));

        //when
        //then
        mockMvc.perform(get("/auth/email-auth").with(csrf())
                        .param("id", "uuid"))
                .andExpect(jsonPath("$.code").value("NOT_EXIST_USER"))
                .andExpect(status().isOk());

    }

    @Test
    @WithMockUser(username="user@naver.com",roles={"USER"})
    @DisplayName("이메일 인증 - 성공")
    void emailAuthSuccess() throws Exception {
        //given
        Member member = new Member();
        member.setEmailAuthStatus(EmailAuthStatus.COMPLETE);
        member.setMemberStatus(MemberStatus.ING);
        member.setEmailAuthDateTime(LocalDateTime.now());
        member.setUsername("user@naver.com");

        given(memberService.emailAuth(anyString())).willReturn(member);

        //when
        //then
        mockMvc.perform(get("/auth/email-auth").with(csrf())
                        .param("id", "uuid"))
                .andExpect(jsonPath("$.username").value("user@naver.com"))
                .andExpect(status().isOk());

    }
}