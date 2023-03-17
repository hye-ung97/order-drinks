package com.zerobase.order_drinks.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.order_drinks.exception.CustomException;
import com.zerobase.order_drinks.model.constants.OrderStatus;
import com.zerobase.order_drinks.model.dto.Auth;
import com.zerobase.order_drinks.model.dto.OrderBillDto;
import com.zerobase.order_drinks.model.entity.Wallet;
import com.zerobase.order_drinks.security.SecurityConfiguration;
import com.zerobase.order_drinks.security.TokenProvider;
import com.zerobase.order_drinks.service.MemberService;
import com.zerobase.order_drinks.service.OrderService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zerobase.order_drinks.exception.ErrorCode.NOT_EXIST_ORDER_LIST;
import static com.zerobase.order_drinks.exception.ErrorCode.NOT_EXIST_USER;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = MemberController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfiguration.class)}
)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenProvider tokenProvider;
    @MockBean
    private MemberService memberService;
    @MockBean
    private OrderService orderService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username="user",roles={"USER"})
    @DisplayName("회원탈퇴 - 실패")
    void withdrawFail() throws Exception {
        //given
        Auth.SignIn login = new Auth.SignIn();
        login.setUsername("user");
        login.setPassword("123");
        given(memberService.withdraw(any())).willThrow(new CustomException(NOT_EXIST_USER));

        //when
        //then
        mockMvc.perform(get("/member/withdraw").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(jsonPath("$.code").value("NOT_EXIST_USER"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="user",roles={"USER"})
    @DisplayName("회원탈퇴 - 성공")
    void withdrawSuccess() throws Exception {
        //given
        Auth.SignIn login = new Auth.SignIn();
        login.setUsername("user");
        login.setPassword("123");

        Map<String, String> result = new HashMap<>();
        result.put("id", login.getUsername());
        result.put("result", "withdraw success!");

        given(memberService.withdraw(any())).willReturn(result);

        //when
        //then
        mockMvc.perform(get("/member/withdraw").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(jsonPath("$.id").value("user"))
                .andExpect(jsonPath("$.result").value("withdraw success!"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="user",roles={"USER"})
    @DisplayName("지갑 보기 - 실패")
    void getWalletFail() throws Exception {
        //given
        given(memberService.getWallet(anyString()))
                .willThrow(new CustomException(NOT_EXIST_USER));

        //when
        //then
        mockMvc.perform(get("/member/wallet").with(csrf()))
                .andExpect(jsonPath("$.code").value("NOT_EXIST_USER"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="user",roles={"USER"})
    @DisplayName("지갑 보기 - 성공")
    void getWalletSuccess() throws Exception {
        //given
        Wallet wallet = new Wallet();
        wallet.setCard(new Wallet.Card());
        wallet.setCoupon(new Wallet.Coupon());
        wallet.setPoint(new Wallet.Point());
        wallet.getCard().setPrice(1000);
        wallet.getPoint().setCount(1);
        wallet.getCoupon().setCount(1);


        given(memberService.getWallet(anyString())).willReturn(wallet);

        //when
        //then
        mockMvc.perform(get("/member/wallet").with(csrf()))
                .andExpect(jsonPath("$.card.price").value(1000))
                .andExpect(jsonPath("$.coupon.count").value(1))
                .andExpect(jsonPath("$.point.count").value(1))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="user",roles={"USER"})
    @DisplayName("카드 충전 - 실패")
    void chargeFail() throws Exception {
        //given
        given(memberService.cardCharge(anyInt(), anyString()))
                .willThrow(new CustomException(NOT_EXIST_USER));
        //when
        //then
        mockMvc.perform(get("/member/charge").with(csrf())
                        .param("price", "5000"))
                .andExpect(jsonPath("$.code").value("NOT_EXIST_USER"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="user",roles={"USER"})
    @DisplayName("카드 충전 - 성공")
    void chargeSuccess() throws Exception {
        //given
        Wallet wallet = new Wallet();
        wallet.setCard(new Wallet.Card());
        wallet.getCard().setPrice(5000);


        given(memberService.cardCharge(anyInt(), anyString()))
                .willReturn(wallet.getCard());
        //when
        //then
        mockMvc.perform(get("/member/charge").with(csrf())
                        .param("price", "5000"))
                .andExpect(jsonPath("$.price").value(5000))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="user",roles={"USER"})
    @DisplayName("주문 리스트 가져오기 - 실패")
    void getUserOrderFail() throws Exception {
        //given
        given(orderService.getUserOrderList(anyString(), any()))
                .willThrow(new CustomException(NOT_EXIST_ORDER_LIST));

        //when
        //then
        mockMvc.perform(get("/member/myOrder").with(csrf()))
                .andExpect(jsonPath("$.code").value("NOT_EXIST_ORDER_LIST"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="user",roles={"USER"})
    @DisplayName("주문 리스트 가져오기 - 성공")
    void getUserOrderSuccess() throws Exception {
        //given
        OrderBillDto orderBillDto1 = new OrderBillDto();
        orderBillDto1.setTotalPrice(4100);
        orderBillDto1.setItem("아메리카노");
        orderBillDto1.setQuantity(1);
        orderBillDto1.setStatus(OrderStatus.ING);
        orderBillDto1.setStoreName("스타벅스1");

        List<OrderBillDto> list = List.of(orderBillDto1);
        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "name";
        Sort sort = Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<OrderBillDto> page = new PageImpl<>(list, pageable, 10);

        given(orderService.getUserOrderList(anyString(), any())).willReturn(page);
        //when
        //then
        mockMvc.perform(get("/member/myOrder").with(csrf()))
                .andExpect(jsonPath("$.content[0].totalPrice").value(4100))
                .andExpect(status().isOk());

    }
}