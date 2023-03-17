package com.zerobase.order_drinks.controller;

import com.zerobase.order_drinks.model.constants.EmailAuthStatus;
import com.zerobase.order_drinks.model.constants.MemberStatus;
import com.zerobase.order_drinks.model.entity.MemberEntity;
import com.zerobase.order_drinks.model.entity.Wallet;
import com.zerobase.order_drinks.security.SecurityConfiguration;
import com.zerobase.order_drinks.security.TokenProvider;
import com.zerobase.order_drinks.service.AdminService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = AdminController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfiguration.class)}
)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AdminService adminService;
    @MockBean
    private TokenProvider tokenProvider;

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    @DisplayName("회원리스트 - 없음")
    void memberListEmpty() throws Exception {
        //given
        given(adminService.getMemberList(any(Pageable.class))).willReturn(Page.empty());

        //when
        //then
        mockMvc.perform(get("/admin/member/list").with(csrf()))
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(status().isOk());

    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    @DisplayName("회원리스트 - 성공")
    void memberListSuccess() throws Exception {
        //given
        List<MemberEntity> list = List.of(
                MemberEntity.builder()
                        .id(1L)
                        .username("user1@naver.com")
                        .memberStatus(MemberStatus.ING)
                        .emailAuthStatus(EmailAuthStatus.COMPLETE)
                        .emailAuthDateTime(LocalDateTime.now())
                        .emailAuthKey("uuid")
                        .card(new Wallet.Card())
                        .coupon(new Wallet.Coupon())
                        .point(new Wallet.Point())
                        .build(),
                MemberEntity.builder()
                        .id(1L)
                        .username("user2@naver.com")
                        .memberStatus(MemberStatus.WITHDRAW)
                        .emailAuthStatus(EmailAuthStatus.COMPLETE)
                        .emailAuthDateTime(LocalDateTime.now())
                        .emailAuthKey("uuid")
                        .card(new Wallet.Card())
                        .coupon(new Wallet.Coupon())
                        .point(new Wallet.Point())
                        .build()
        );
        Page<MemberEntity> page = new PageImpl<>(list);
        given(adminService.getMemberList(any(Pageable.class))).willReturn(page);

        //when
        //then
        mockMvc.perform(get("/admin/member/list").with(csrf()))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.[0].username").value("user1@naver.com"))
                .andExpect(jsonPath("$.content.[1].username").value("user2@naver.com"))
                .andExpect(status().isOk());

    }
}