package com.zerobase.order_drinks.service;

import com.zerobase.order_drinks.components.MailComponent;
import com.zerobase.order_drinks.exception.CustomException;
import com.zerobase.order_drinks.model.constants.EmailAuthStatus;
import com.zerobase.order_drinks.model.constants.MemberStatus;
import com.zerobase.order_drinks.model.dto.Auth;
import com.zerobase.order_drinks.model.entity.MemberEntity;
import com.zerobase.order_drinks.model.entity.Wallet;
import com.zerobase.order_drinks.repository.CardRepository;
import com.zerobase.order_drinks.repository.CouponRepository;
import com.zerobase.order_drinks.repository.MemberRepository;
import com.zerobase.order_drinks.repository.PointRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.zerobase.order_drinks.exception.ErrorCode.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {
    @InjectMocks
    private MemberService memberService;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private CouponRepository couponRepository;
    @Mock
    private PointRepository pointRepository;
    @Mock
    private MailComponent mailComponent;
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
    @DisplayName("login - 성공")
    void loadUserByUsernameSuccess(){
        //given
        MemberEntity member = MemberEntity.builder()
                .id(1L)
                .username("user@naver.com")
                .password(passwordEncoder.encode("123"))
                .emailAuthStatus(EmailAuthStatus.COMPLETE)
                .emailAuthDateTime(LocalDateTime.now())
                .emailAuthKey("uuid")
                .card(new Wallet.Card())
                .coupon(new Wallet.Coupon())
                .point(new Wallet.Point())
                .build();

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.ofNullable(member));

        //when
        var result = memberService.loadUserByUsername("user@naver.com");

        //then
        assertEquals("user@naver.com", result.getUsername());

    }

    @Test
    @DisplayName("login - 실패")
    void loadUserByUsernameFail() {
        //given
        given(memberRepository.findByUsername(anyString())).willReturn(Optional.empty());

        //when
        UsernameNotFoundException exception = assertThrows(UsernameNotFoundException.class,
                () -> memberService.loadUserByUsername("user@naver.com"));

        //then
        assertEquals("couldn`t find user -> user@naver.com", exception.getMessage());
    }

    @Test
    @DisplayName("회원가입 - 실패 - 사용자존재")
    void registerFailUser() {
        //given
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_USER");

        Auth.SignUp signUp = new Auth.SignUp();
        signUp.setPassword("1234");
        signUp.setUsername("user@naver.com");
        signUp.setRoles(roles);

        given(memberRepository.existsByUsername(anyString())).willReturn(true);

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> memberService.register(signUp));

        //then
        assertEquals(ALREADY_EXIST_USER, exception.getErrorCode());
    }

    @Test
    @DisplayName("회원가입 - 실패 - 이메일형식 아님")
    void registerFailEmail() {
        //given
        Auth.SignUp signUp = new Auth.SignUp();
        signUp.setPassword("1234");
        signUp.setUsername("user");

        given(memberRepository.existsByUsername(anyString())).willReturn(false);

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> memberService.register(signUp));

        //then
        assertEquals(NO_EMAIL_PATTERN, exception.getErrorCode());
    }

    @Test
    @DisplayName("회원가입 - 성공")
    void registerFailSuccess() {
        //given
        List<String> roles = new ArrayList<>();
        roles.add("ROLE_USER");
        Auth.SignUp signUp = new Auth.SignUp();
        signUp.setPassword("1234");
        signUp.setUsername("user@naver.com");
        signUp.setRoles(roles);

        MemberEntity member = MemberEntity.builder()
                .id(1L)
                .username("user@naver.com")
                .password(passwordEncoder.encode("1234"))
                .roles(roles)
                .emailAuthStatus(EmailAuthStatus.ING)
                .emailAuthDateTime(LocalDateTime.now())
                .emailAuthKey("uuid")
                .card(new Wallet.Card())
                .coupon(new Wallet.Coupon())
                .point(new Wallet.Point())
                .build();

        given(memberRepository.existsByUsername(anyString())).willReturn(false);
        given(memberRepository.save(any())).willReturn(member);

        //when
        var result = memberService.register(signUp);

        //then
        assertEquals(signUp.getUsername(), result.getUsername());
        assertEquals(signUp.getRoles(), result.getRoles());
        assertEquals("uuid", result.getEmailAuthKey());
        assertEquals(EmailAuthStatus.ING, result.getEmailAuthStatus());
    }

    @Test
    @DisplayName("인증 - 실패 - 사용자")
    void authenticateFailUser() {
        //given
        Auth.SignIn login = new Auth.SignIn();
        login.setUsername("user@naver.com");
        login.setPassword("1234");

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.empty());

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> memberService.authenticate(login));

        //then
        assertEquals(NOT_EXIST_USER, exception.getErrorCode());
    }

    @Test
    @DisplayName("인증 - 실패 - 비밀번호")
    void authenticateFailPassword() {
        //given
        Auth.SignIn login = new Auth.SignIn();
        login.setUsername("user@naver.com");
        login.setPassword("123");

        MemberEntity member = MemberEntity.builder()
                .id(1L)
                .username("user@naver.com")
                .password(passwordEncoder.encode("124"))
                .emailAuthStatus(EmailAuthStatus.ING)
                .emailAuthDateTime(LocalDateTime.now())
                .memberStatus(MemberStatus.REQ)
                .emailAuthKey("uuid")
                .card(new Wallet.Card())
                .coupon(new Wallet.Coupon())
                .point(new Wallet.Point())
                .build();

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.ofNullable(member));

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> memberService.authenticate(login));

        //then
        assertEquals(PASSWORD_NOT_MATCH, exception.getErrorCode());
    }

    @Test
    @DisplayName("인증 - 실패 - 이메일")
    void authenticateFailEmail() {
        //given
        Auth.SignIn login = new Auth.SignIn();
        login.setUsername("user@naver.com");
        login.setPassword("123");

        MemberEntity member = MemberEntity.builder()
                .id(1L)
                .username("user1@naver.com")
                .password(passwordEncoder.encode("123"))
                .emailAuthStatus(EmailAuthStatus.ING)
                .emailAuthDateTime(LocalDateTime.now())
                .emailAuthKey("uuid")
                .card(new Wallet.Card())
                .coupon(new Wallet.Coupon())
                .point(new Wallet.Point())
                .build();

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.ofNullable(member));

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> memberService.authenticate(login));

        //then
        assertEquals(NO_EMAIL_AUTH, exception.getErrorCode());
    }

    @Test
    @DisplayName("인증 - 실패 - 회원탈퇴")
    void authenticateFailWithdraw() {
        //given
        Auth.SignIn login = new Auth.SignIn();
        login.setUsername("user@naver.com");
        login.setPassword("123");

        MemberEntity member = MemberEntity.builder()
                .id(1L)
                .username("user1@naver.com")
                .password(passwordEncoder.encode("123"))
                .emailAuthStatus(EmailAuthStatus.COMPLETE)
                .emailAuthDateTime(LocalDateTime.now())
                .emailAuthKey("uuid")
                .card(new Wallet.Card())
                .coupon(new Wallet.Coupon())
                .point(new Wallet.Point())
                .memberStatus(MemberStatus.WITHDRAW)
                .build();

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.ofNullable(member));

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> memberService.authenticate(login));

        //then
        assertEquals(WITHDRAW_USER, exception.getErrorCode());
    }

    @Test
    @DisplayName("이메일 인증 - 실패")
    void emailAuthFail() {
        //given
        given(memberRepository.findByEmailAuthKey(anyString())).willReturn(Optional.empty());

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> memberService.emailAuth("uuid"));

        //then
        assertEquals(NOT_EXIST_USER, exception.getErrorCode());

    }

    @Test
    @DisplayName("이메일 인증 - 성공")
    void emailAuthSuccess() {
        //given
        MemberEntity member = MemberEntity.builder()
                .id(1L)
                .username("user@naver.com")
                .password("1234")
                .emailAuthStatus(EmailAuthStatus.ING)
                .emailAuthDateTime(LocalDateTime.now())
                .emailAuthKey("uuid")
                .card(new Wallet.Card())
                .coupon(new Wallet.Coupon())
                .point(new Wallet.Point())
                .build();

        given(memberRepository.findByEmailAuthKey(anyString())).willReturn(Optional.ofNullable(member));

        //when
        var result = memberService.emailAuth("uuid");

        //then
        assertEquals(EmailAuthStatus.COMPLETE, result.getEmailAuthStatus());

    }

    @Test
    @DisplayName("회원탈퇴")
    void withdraw() {
        //given
        Auth.SignIn login = new Auth.SignIn();
        login.setUsername("user@naver.com");
        login.setPassword("123");

        MemberEntity member = MemberEntity.builder()
                .id(1L)
                .username("user@naver.com")
                .password(passwordEncoder.encode("123"))
                .memberStatus(MemberStatus.ING)
                .emailAuthStatus(EmailAuthStatus.COMPLETE)
                .emailAuthDateTime(LocalDateTime.now())
                .emailAuthKey("uuid")
                .card(new Wallet.Card())
                .coupon(new Wallet.Coupon())
                .point(new Wallet.Point())
                .build();

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.ofNullable(member));
        given(memberRepository.save(any())).willReturn(member);

        //when

        var result = memberService.withdraw(login);

        //then
        assertEquals(login.getUsername(), result.get("id"));
    }

    @Test
    @DisplayName("카드 충전 - 실패")
    void cardChargeFail() {
        //given
        given(memberRepository.findByUsername(anyString()))
                .willReturn(Optional.empty());

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> memberService.cardCharge(15000, "user@naver.com"));

        //then
        assertEquals(NOT_EXIST_USER, exception.getErrorCode());
    }

    @Test
    @DisplayName("카드 충전 - 성공")
    void cardChargeSuccess() {
        //given
        MemberEntity member = MemberEntity.builder()
                .id(1L)
                .username("user@naver.com")
                .password("1234")
                .emailAuthStatus(EmailAuthStatus.COMPLETE)
                .emailAuthDateTime(LocalDateTime.now())
                .card(new Wallet.Card())
                .coupon(new Wallet.Coupon())
                .point(new Wallet.Point())
                .build();

        member.getCard().setPrice(1000);

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));

        //when
        var result = memberService.cardCharge(1000, member.getUsername());

        //then
        assertEquals(2000, result.getPrice());
    }

    @Test
    @DisplayName("지갑 가져오기 - 실패 - 사용자 없음")
    void getWalletFail() {
        //given
        given(memberRepository.findByUsername(anyString()))
                .willReturn(Optional.empty());

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> memberService.getWallet("user@naver.com"));

        //then
        assertEquals(NOT_EXIST_USER, exception.getErrorCode());
    }

    @Test
    @DisplayName("지갑 가져오기 - 성공")
    void getWalletSuccess() {
        //given
        MemberEntity member = MemberEntity.builder()
                .id(1L)
                .username("admin@naver.com")
                .password("1234")
                .emailAuthStatus(EmailAuthStatus.COMPLETE)
                .emailAuthDateTime(LocalDateTime.now())
                .card(new Wallet.Card())
                .coupon(new Wallet.Coupon())
                .point(new Wallet.Point())
                .build();

        member.getCard().setPrice(15000);
        member.getCoupon().setCount(2);
        member.getPoint().setCount(7);

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));

        //when
        var result = memberService.getWallet(member.getUsername());

        //then
        assertEquals(15000, result.getCard().getPrice());
        assertEquals(2, result.getCoupon().getCount());
        assertEquals(7, result.getPoint().getCount());
    }
}