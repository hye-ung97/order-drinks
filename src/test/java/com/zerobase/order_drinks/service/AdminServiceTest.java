package com.zerobase.order_drinks.service;

import com.zerobase.order_drinks.model.constants.EmailAuthStatus;
import com.zerobase.order_drinks.model.entity.MemberEntity;
import com.zerobase.order_drinks.model.entity.Wallet;
import com.zerobase.order_drinks.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;


@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    @InjectMocks
    private AdminService adminService;
    @Mock
    private MemberRepository memberRepository;

    @Test
    @DisplayName("회원리스트 가져오기 - 회원없음")
    void getMemberListFail(){
        //given
        given(memberRepository.findAll(any(Pageable.class))).willReturn(Page.empty());
        //when
        var result = memberRepository.findAll(Pageable.ofSize(10));
        //then
        assertEquals(0, result.getSize());
    }
    @Test
    @DisplayName("회원리스트 가져오기 - 성공")
    void getMemberListSuccess() {
        //given
        List<MemberEntity> list = List.of(
                MemberEntity.builder()
                        .id(1L)
                        .username("user@naver.com")
                        .emailAuthStatus(EmailAuthStatus.ING)
                        .emailAuthDateTime(LocalDateTime.now())
                        .emailAuthKey("uuid")
                        .card(new Wallet.Card())
                        .coupon(new Wallet.Coupon())
                        .point(new Wallet.Point())
                        .build(),
                MemberEntity.builder()
                        .id(2L)
                        .username("user2@naver.com")
                        .emailAuthStatus(EmailAuthStatus.COMPLETE)
                        .emailAuthDateTime(LocalDateTime.now())
                        .emailAuthKey("uuid")
                        .card(new Wallet.Card())
                        .coupon(new Wallet.Coupon())
                        .point(new Wallet.Point())
                        .build()
        );
        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "name";
        Sort sort = Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<MemberEntity> page = new PageImpl<>(list, pageable,10);
        given(memberRepository.findAll(any(Pageable.class))).willReturn(page);

        //when
        var result = adminService.getMemberList(pageable);

        //then
        assertEquals("user@naver.com",
                result.get().collect(Collectors.toList()).get(0).getUsername());
        assertEquals("user2@naver.com",
                result.get().collect(Collectors.toList()).get(1).getUsername());
    }
}