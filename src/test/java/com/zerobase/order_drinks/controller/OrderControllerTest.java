package com.zerobase.order_drinks.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.order_drinks.exception.CustomException;
import com.zerobase.order_drinks.model.constants.OrderStatus;
import com.zerobase.order_drinks.model.constants.Pay;
import com.zerobase.order_drinks.model.dto.*;
import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import com.zerobase.order_drinks.notification.NotificationService;
import com.zerobase.order_drinks.security.SecurityConfiguration;
import com.zerobase.order_drinks.security.TokenProvider;
import com.zerobase.order_drinks.service.GoogleMapService;
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

import java.time.LocalDateTime;
import java.util.List;

import static com.zerobase.order_drinks.exception.ErrorCode.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = OrderController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfiguration.class)}
)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenProvider tokenProvider;
    @MockBean
    private OrderService orderService;
    @MockBean
    private GoogleMapService googleMapService;
    @MockBean
    private NotificationService notificationService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username="user",roles={"USER"})
    void findLocation() throws Exception {
        //given
        List<StoreData> list = List.of(
                StoreData.builder()
                        .address("대한민국 노량진로 134 동작구 서울특별시 KR")
                        .distance("0.686 km")
                        .storeName("스타벅스 노량진역점")
                        .build(),
                StoreData.builder()
                        .address("대한민국 서울특별시 영등포구 국제금융로 86")
                        .distance("0.741 km")
                        .storeName("스타벅스 동여의도점")
                        .build());
        given(googleMapService.findStoreFromApi(any())).willReturn(list);

        //when

        //then
        mockMvc.perform(get("/order/find-store").with(csrf())
                        .param("address","서울특별시 영등포구 63로 50"))
                .andExpect(jsonPath("$.[0].storeName")
                        .value("스타벅스 노량진역점"))
                .andExpect(jsonPath("$.[0].address")
                        .value("대한민국 노량진로 134 동작구 서울특별시 KR"))
                .andExpect(jsonPath("$.[0].distance")
                        .value("0.686 km"))
                .andExpect(jsonPath("$.[1].storeName")
                        .value("스타벅스 동여의도점"))
                .andExpect(jsonPath("$.[1].address")
                        .value("대한민국 서울특별시 영등포구 국제금융로 86"))
                .andExpect(jsonPath("$.[1].distance")
                        .value("0.741 km"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="user",roles={"USER"})
    @DisplayName("주문 실패")
    void orderFail() throws Exception {
        //given
        Order order = new Order();
        order.setItem("아메리카노");
        order.setPay(Pay.CARD);
        order.setStoreName("스타벅스");
        order.setQuantity(1);

        given(orderService.orderReceipt(any(),any()))
                .willThrow(new CustomException(NOT_EXIST_MENU));

        //when

        //then
        mockMvc.perform(post("/order").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(jsonPath("$.code")
                        .value("NOT_EXIST_MENU"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="user",roles={"USER"})
    @DisplayName("주문 성공")
    void orderSuccess() throws Exception {
        //given
        Order order = new Order();
        order.setItem("아메리카노");
        order.setPay(Pay.CARD);
        order.setStoreName("스타벅스");
        order.setQuantity(1);

        OrderBillDto orderBillDto = new OrderBillDto();
        orderBillDto.setItem("아메리카노");
        orderBillDto.setOrderTime(LocalDateTime.now());
        orderBillDto.setStoreName("스타벅스");
        orderBillDto.setPay(Pay.CARD);
        orderBillDto.setQuantity(1);
        orderBillDto.setStatus(OrderStatus.ING);
        orderBillDto.setTotalPrice(4100);

        given(orderService.orderReceipt(any(),any())).willReturn(orderBillDto);

        //when

        //then
        mockMvc.perform(post("/order").with(csrf()).contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(jsonPath("$.totalPrice")
                        .value(4100))
                .andExpect(jsonPath("$.item")
                        .value("아메리카노"))
                .andExpect(jsonPath("$.pay")
                        .value(Pay.CARD.toString()))
                .andExpect(jsonPath("$.status")
                        .value(OrderStatus.ING.toString()))
                .andExpect(jsonPath("$.quantity")
                        .value(1))
                .andExpect(jsonPath("$.storeName")
                        .value("스타벅스"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    @DisplayName("음료 상태보기 - 실패")
    void orderStatusFail() throws Exception {
        //given
        given(orderService.checkStatus(any(), any()))
                .willThrow(new CustomException(NOT_EXIST_ORDER_LIST));

        //when
        //then
        mockMvc.perform(get("/order/list/ING").with(csrf()))
                .andExpect(jsonPath("$.code")
                        .value("NOT_EXIST_ORDER_LIST"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    @DisplayName("음료 상태보기 - 성공")
    void orderStatusSuccess() throws Exception {
        //given
        List<ListOrderEntity> lists = List.of(ListOrderEntity.builder()
                .orderStatus(OrderStatus.ING)
                .orderDateTime(LocalDateTime.now())
                .pay(Pay.CARD)
                .price(4100)
                .menu("아메리카노")
                .quantity(1)
                .userName("user1")
                .build(),
                ListOrderEntity.builder()
                        .orderStatus(OrderStatus.ING)
                        .orderDateTime(LocalDateTime.now())
                        .pay(Pay.CARD)
                        .price(4500)
                        .menu("라떼")
                        .quantity(1)
                        .userName("user2")
                        .build());

        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "name";
        Sort sort = Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<ListOrderEntity> page = new PageImpl<>(lists, pageable, 10);

        given(orderService.checkStatus(any(), any())).willReturn(page);

        //when
        //then
        mockMvc.perform(get("/order/list/ING").with(csrf()))
                .andExpect(jsonPath("$.content[0].userName")
                        .value("user1"))
                .andExpect(jsonPath("$.content[0].orderStatus").
                        value(OrderStatus.ING.toString()))
                .andExpect(jsonPath("$.content[0].pay")
                        .value(Pay.CARD.toString()))
                .andExpect(jsonPath("$.content[0].price")
                        .value(4100))
                .andExpect(jsonPath("$.content[0].menu")
                        .value("아메리카노"))
                .andExpect(jsonPath("$.content[0].quantity")
                        .value(1))

                .andExpect(jsonPath("$.content[1].userName")
                        .value("user2"))
                .andExpect(jsonPath("$.content[1].orderStatus")
                        .value(OrderStatus.ING.toString()))
                .andExpect(jsonPath("$.content[1].pay")
                        .value(Pay.CARD.toString()))
                .andExpect(jsonPath("$.content[1].price")
                        .value(4500))
                .andExpect(jsonPath("$.content[1].menu")
                        .value("라떼"))
                .andExpect(jsonPath("$.content[1].quantity")
                        .value(1))
                .andExpect(status().isOk());

    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    @DisplayName("음료 상태 변경 - 실패")
    void orderStatusChangeFail() throws Exception {
        //given
        given(orderService.changeOrderStatus(anyInt()))
                .willThrow(new CustomException(ALREADY_FINISHED_DRINK));

        //when
        //then
        mockMvc.perform(get("/order/status-change").with(csrf())
                        .param("orderNo", "1"))
                .andExpect(jsonPath("$.code")
                        .value("ALREADY_FINISHED_DRINK"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    @DisplayName("음료 상태 변경 - 성공")
    void orderStatusChangeSuccess() throws Exception {
        //given
        ListOrderEntity entity = ListOrderEntity.builder()
                                        .orderStatus(OrderStatus.COMPLETE)
                                        .orderDateTime(LocalDateTime.now())
                                        .pay(Pay.CARD)
                                        .price(4100)
                                        .menu("아메리카노")
                                        .quantity(1)
                                        .userName("user1")
                                        .build();

        given(orderService.changeOrderStatus(anyInt())).willReturn(entity);

        //when
        //then
        mockMvc.perform(get("/order/status-change").with(csrf())
                        .param("orderNo", "1"))
                .andExpect(jsonPath("$.orderStatus").value(OrderStatus.COMPLETE.toString()))
                .andExpect(jsonPath("$.userName").value("user1"))
                .andExpect(jsonPath("$.quantity").value(1))
                .andExpect(jsonPath("$.menu").value("아메리카노"))
                .andExpect(jsonPath("$.price").value(4100))
                .andExpect(jsonPath("$.pay").value(Pay.CARD.toString()))
                .andExpect(status().isOk());
       verify(notificationService, times(1))
               .send(any(), any(), anyInt());
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    @DisplayName("기간별 주문 리스트 - 실패")
    void orderListByTermFail() throws Exception {
        //given
        given(orderService.getOrderList(any(), any(), any()))
                .willThrow(new CustomException(NOT_EXIST_ORDER_LIST));

        //when
        //then
        mockMvc.perform(get("/order/list").with(csrf())
                .param("startDate","2023-03-01")
                .param("endDate", "2023-03-31"))
                .andExpect(jsonPath("$.code")
                        .value("NOT_EXIST_ORDER_LIST"));
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    @DisplayName("기간별 주문 리스트 - 성공")
    void orderListByTermSuccess() throws Exception {
        //given
        List<ListOrderEntity> orderList = List.of(
                ListOrderEntity.builder()
                        .userName("user1")
                        .store("스타벅스1")
                        .orderStatus(OrderStatus.COMPLETE)
                        .no(1)
                        .quantity(1)
                        .menu("아메리카노")
                        .pay(Pay.CARD)
                        .price(4100)
                        .build(),
                ListOrderEntity.builder()
                        .userName("user2")
                        .store("스타벅스1")
                        .orderStatus(OrderStatus.COMPLETE)
                        .no(2)
                        .quantity(2)
                        .menu("아메리카노")
                        .pay(Pay.CARD)
                        .price(8200)
                        .build()
        );
        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "name";
        Sort sort = Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(pageNumber, pageSize, sort);

        Page<ListOrderEntity> page = new PageImpl<>(orderList, pageable, 10);
        given(orderService.getOrderList(any(), any(), any())).willReturn(page);

        //when
        //then
        mockMvc.perform(get("/order/list").with(csrf())
                        .param("startDate","2023-03-01")
                        .param("endDate", "2023-03-31"))
                .andExpect(jsonPath("$.content[0].userName")
                        .value("user1"))
                .andExpect(jsonPath("$.content[0].store")
                        .value("스타벅스1"))
                .andExpect(jsonPath("$.content[0].pay")
                        .value(Pay.CARD.toString()))
                .andExpect(jsonPath("$.content[0].price")
                        .value(4100))
                .andExpect(jsonPath("$.content[0].quantity")
                        .value(1))
                .andExpect(jsonPath("$.content[1].userName")
                        .value("user2"))
                .andExpect(jsonPath("$.content[1].store")
                        .value("스타벅스1"))
                .andExpect(jsonPath("$.content[1].pay")
                        .value(Pay.CARD.toString()))
                .andExpect(jsonPath("$.content[1].price")
                        .value(8200))
                .andExpect(jsonPath("$.content[1].quantity")
                        .value(2))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    @DisplayName("지점 주문 리스트 보기(기간별) - 실패")
    void orderListByStoreFail() throws Exception {
        //given
        given(orderService.getOrderListByStoreName(any(), any(), any(), any()))
                .willThrow(new CustomException(NOT_FOUND_STORE_DATA));
        //when
        //then
        mockMvc.perform(get("/order/list-store").with(csrf())
                        .param("storeName", "스타벅스1")
                        .param("startDate", "2023-03-01")
                        .param("endDate", "2023-03-31"))
                .andExpect(jsonPath("$.code")
                        .value("NOT_FOUND_STORE_DATA"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    @DisplayName("지점 주문 리스트 보기(기간별) - 성공")
    void orderListByStoreSuccess() throws Exception {
        //given
        List<ListOrderEntity> orderList = List.of(
                ListOrderEntity.builder()
                        .userName("user1")
                        .store("스타벅스1")
                        .orderStatus(OrderStatus.COMPLETE)
                        .no(1)
                        .quantity(1)
                        .menu("아메리카노")
                        .pay(Pay.CARD)
                        .price(4100)
                        .build(),
                ListOrderEntity.builder()
                        .userName("user2")
                        .store("스타벅스2")
                        .orderStatus(OrderStatus.COMPLETE)
                        .no(2)
                        .quantity(2)
                        .menu("아메리카노")
                        .pay(Pay.CARD)
                        .price(8200)
                        .build()
        );

        long sum = orderList.stream().mapToLong(ListOrderEntity::getPrice).sum();
        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "name";
        Sort sort = Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<ListOrderEntity> page = new PageImpl<>(orderList, pageable, 10);

        StoreOrderBillDto storeOrderBillDto = new StoreOrderBillDto();
        storeOrderBillDto.setSum(sum);
        storeOrderBillDto.setOrderList(page);

        given(orderService.getOrderListByStoreName(any(), any(), any(), any()))
                .willReturn(storeOrderBillDto);
        //when
        //then
        mockMvc.perform(get("/order/list-store").with(csrf())
                        .param("storeName", "스타벅스1")
                        .param("startDate", "2023-03-01")
                        .param("endDate", "2023-03-31"))
                .andExpect(jsonPath("$.sum").value(12300))
                .andExpect(jsonPath("$.orderList.content[0].price")
                        .value(4100))
                .andExpect(jsonPath("$.orderList.content[0].userName")
                        .value("user1"))
                .andExpect(jsonPath("$.orderList.content[1].price")
                        .value(8200))
                .andExpect(jsonPath("$.orderList.content[1].userName")
                        .value("user2"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    @DisplayName("각각의 지점별 판매 금액 보기(기간별) - 실패")
    void eachStorePriceListFail() throws Exception {
        //given
        given(orderService.getEachStoreSalesPrice(any(), any(), any()))
                .willThrow( new CustomException(NOT_EXIST_STORE_SALES_DATA));

        //when
        //then
        mockMvc.perform(get("/order/list-each").with(csrf())
                .param("startDate", "2023-03-01")
                .param("endDate", "2023-03-31"))
                .andExpect(jsonPath("$.code")
                        .value("NOT_EXIST_STORE_SALES_DATA"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    @DisplayName("각각의 지점별 판매 금액 보기(기간별) - 성공")
    void eachStorePriceListSuccess() throws Exception {
        //given
        List<StoreGroupDto> list = List.of(
                new StoreGroupDto() {
                    @Override
                    public String getStoreName() {
                        return "스타벅스1";
                    }

                    @Override
                    public long getTotalPrice() {
                        return 4100;
                    }
                }, new StoreGroupDto() {
                    @Override
                    public String getStoreName() {
                        return "스타벅스2";
                    }

                    @Override
                    public long getTotalPrice() {
                        return 8200;
                    }
                }
        );

        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "name";
        Sort sort = Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(pageNumber, pageSize, sort);
        Page<StoreGroupDto> page = new PageImpl<>(list, pageable, 10);

        given(orderService.getEachStoreSalesPrice(any(), any(), any()))
                .willReturn(page);
        //when
        //then
        mockMvc.perform(get("/order/list-each").with(csrf())
                        .param("startDate", "2023-03-01")
                        .param("endDate", "2023-03-31"))
                .andExpect(jsonPath("$.content[0].totalPrice")
                        .value(4100))
                .andExpect(jsonPath("$.content[0].storeName")
                        .value("스타벅스1"))
                .andExpect(jsonPath("$.content[1].totalPrice")
                        .value(8200))
                .andExpect(jsonPath("$.content[1].storeName")
                        .value("스타벅스2"))
                .andExpect(status().isOk());
    }
}