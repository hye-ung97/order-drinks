package com.zerobase.order_drinks.service;

import com.zerobase.order_drinks.exception.CustomException;
import com.zerobase.order_drinks.model.constants.EmailAuthStatus;
import com.zerobase.order_drinks.model.constants.OrderStatus;
import com.zerobase.order_drinks.model.constants.Pay;
import com.zerobase.order_drinks.model.dto.Order;
import com.zerobase.order_drinks.model.dto.OrderBillDto;
import com.zerobase.order_drinks.model.entity.*;
import com.zerobase.order_drinks.repository.ListOrderRepository;
import com.zerobase.order_drinks.repository.MemberRepository;
import com.zerobase.order_drinks.repository.MenuRepository;
import com.zerobase.order_drinks.repository.StoreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.zerobase.order_drinks.exception.ErrorCode.*;
import static com.zerobase.order_drinks.model.constants.OrderStatus.COMPLETE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private MenuRepository menuRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private ListOrderRepository listOrderRepository;
    @Mock
    private StoreRepository storeRepository;

    @InjectMocks
    private OrderService orderService;



    @Test
    @DisplayName("음료 주문 - 실패 - 메뉴 없음")
    void orderReceiptFailNoMenu(){
        //given
        Order order = new Order();
        order.setItem("아메리카노");
        order.setPay(Pay.CARD);
        order.setStoreName("스타벅스");
        order.setQuantity(2);

        given(menuRepository.findByMenuName(anyString())).willReturn(Optional.empty());

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> orderService.orderReceipt(order, "user@naver.com"));

        //then
        assertEquals(NOT_EXIST_MENU, exception.getErrorCode());
    }

    @Test
    @DisplayName("음료 주문 - 실패 - 지점 없음")
    void orderReceiptFailNoStore(){
        //given
        Order order = new Order();
        order.setItem("아메리카노");
        order.setPay(Pay.CARD);
        order.setStoreName("스타벅스");
        order.setQuantity(2);

        MenuEntity menu = MenuEntity.builder()
                .price(4100)
                .menuName("아메리카노")
                .registerDateTime(LocalDateTime.now())
                .build();

        given(menuRepository.findByMenuName(anyString())).willReturn(Optional.of(menu));
        given(storeRepository.findByStoreName(anyString())).willThrow(new CustomException(NOT_FOUND_STORE_DATA));

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> orderService.orderReceipt(order, "user@naver.com"));

        //then
        assertEquals(NOT_FOUND_STORE_DATA, exception.getErrorCode());
    }

    @Test
    @DisplayName("음료 주문 - 실패 - 재고 없음")
    void orderReceiptFailOutOfStock(){
        //given
        Order order = new Order();
        order.setItem("아메리카노");
        order.setPay(Pay.CARD);
        order.setStoreName("스타벅스");
        order.setQuantity(2);

        MenuEntity menu = MenuEntity.builder()
                .price(4100)
                .menuName("아메리카노")
                .registerDateTime(LocalDateTime.now())
                .quantity(1)
                .build();

        StoreEntity store = StoreEntity.builder()
                .storeId(1)
                .storeName("스타벅스1")
                .build();

        given(menuRepository.findByMenuName(anyString())).willReturn(Optional.ofNullable(menu));
        given(storeRepository.findByStoreName(anyString())).willReturn(Optional.ofNullable(store));

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> orderService.orderReceipt(order, "user@naver.com"));

        //then
        assertEquals(OUT_OF_STOCK, exception.getErrorCode());
    }

    @Test
    @DisplayName("음료 주문 - 실패 - 카드 잔액 없음")
    void orderReceiptFailNoCard(){
        //given
        Order order = new Order();
        order.setItem("아메리카노");
        order.setPay(Pay.CARD);
        order.setStoreName("스타벅스");
        order.setQuantity(2);

        MenuEntity menu = MenuEntity.builder()
                .price(4100)
                .menuName("아메리카노")
                .quantity(10)
                .registerDateTime(LocalDateTime.now())
                .build();

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
        member.getCard().setPrice(4100);

        StoreEntity store = StoreEntity.builder()
                .storeId(1)
                .storeName("스타벅스1")
                .build();

        given(menuRepository.findByMenuName(anyString())).willReturn(Optional.of(menu));
        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));
        given(storeRepository.findByStoreName(anyString())).willReturn(Optional.ofNullable(store));

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> orderService.orderReceipt(order, "user@naver.com"));

        //then
        assertEquals(LOW_CARD_PRICE, exception.getErrorCode());
    }

    @Test
    @DisplayName("음료 주문 - 실패 - 쿠폰 없음")
    void orderReceiptFailNoCoupon(){
        Order order = new Order();
        order.setItem("아메리카노");
        order.setPay(Pay.COUPON);
        order.setStoreName("스타벅스");
        order.setQuantity(2);

        MenuEntity menu = MenuEntity.builder()
                .price(4100)
                .menuName("아메리카노")
                .quantity(10)
                .registerDateTime(LocalDateTime.now())
                .build();

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
        member.getCoupon().setCount(0);

        StoreEntity store = StoreEntity.builder()
                .storeId(1)
                .storeName("스타벅스1")
                .build();

        given(menuRepository.findByMenuName(anyString())).willReturn(Optional.of(menu));
        given(storeRepository.findByStoreName(anyString())).willReturn(Optional.ofNullable(store));
        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> orderService.orderReceipt(order, "user@naver.com"));

        //then
        assertEquals(UNAVAILABLE_COUPON, exception.getErrorCode());
    }

    @Test
    @DisplayName("음료 주문 - 성공")
    void orderReceiptSuccess() {
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
                .listOrder(new ArrayList<>())
                .build();

        member.getCard().setPrice(10000);

        Order order = new Order();
        order.setItem("아메리카노");
        order.setPay(Pay.CARD);
        order.setStoreName("스타벅스");
        order.setQuantity(2);

        MenuEntity menu = MenuEntity.builder()
                .menuName("아메리카노")
                .price(4100)
                .quantity(5)
                .registerDateTime(LocalDateTime.now())
                .build();

        given(menuRepository.findByMenuName(anyString())).willReturn(Optional.of(menu));

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));

        given(storeRepository.findByStoreName(anyString())).willReturn(Optional.ofNullable(StoreEntity.builder()
                        .storeName("스타벅스1").build()));

        given(listOrderRepository.save(any())).willReturn(ListOrderEntity.builder()
                .pay(Pay.CARD)
                .orderDateTime(LocalDateTime.now())
                .quantity(2)
                .userName(member)
                .orderCompleteDateTime(null)
                .orderStatus(OrderStatus.ING)
                .price(8200)
                .store(StoreEntity.builder().storeId(1).storeName("스타벅스1").build())
        .build());

        //when
        OrderBillDto orderReceipt = orderService.orderReceipt(order, member.getUsername());

        //then
        assertEquals(8200, orderReceipt.getTotalPrice());
        assertEquals(3, menu.getQuantity());
        assertEquals("스타벅스1", orderReceipt.getStoreName());
    }

    @Test
    @DisplayName("포인트 적립")
    void savePoint(){
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
                .listOrder(new ArrayList<>())
                .build();

        member.getCard().setPrice(10000);

        Order order = new Order();
        order.setItem("아메리카노");
        order.setPay(Pay.CARD);
        order.setStoreName("스타벅스");
        order.setQuantity(2);

        given(menuRepository.findByMenuName(anyString())).willReturn(Optional.of(MenuEntity.builder()
                .menuName("아메리카노")
                .price(4100)
                .quantity(10)
                .registerDateTime(LocalDateTime.now())
                .build()));

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));

        given(storeRepository.findByStoreName(anyString())).willReturn(Optional.ofNullable(StoreEntity.builder()
                .storeName("스타벅스1").build()));

        given(listOrderRepository.save(any())).willReturn(ListOrderEntity.builder()
                .pay(Pay.CARD)
                .orderDateTime(LocalDateTime.now())
                .quantity(2)
                .userName(member)
                .orderCompleteDateTime(null)
                .orderStatus(OrderStatus.ING)
                .price(8200)
                .store(StoreEntity.builder().storeId(1).storeName("스타벅스1").build())
                .build());

        //when
        orderService.orderReceipt(order, member.getUsername());

        //then
        assertEquals(2, member.getPoint().getCount());
    }

    @Test
    @DisplayName("포인트 차감후 쿠폰으로 교환")
    void changeToCoupon(){
        MemberEntity member = MemberEntity.builder()
                .id(1L)
                .username("user@naver.com")
                .password("1234")
                .emailAuthStatus(EmailAuthStatus.COMPLETE)
                .emailAuthDateTime(LocalDateTime.now())
                .card(new Wallet.Card())
                .coupon(new Wallet.Coupon())
                .point(new Wallet.Point())
                .listOrder(new ArrayList<>())
                .build();

        member.getCard().setPrice(100000);
        member.getCoupon().setCount(0);
        member.getPoint().setCount(11);

        Order order = new Order();
        order.setItem("아메리카노");
        order.setPay(Pay.CARD);
        order.setStoreName("스타벅스");
        order.setQuantity(2);

        given(menuRepository.findByMenuName(anyString())).willReturn(Optional.of(MenuEntity.builder()
                .menuName("아메리카노")
                .price(4100)
                .quantity(10)
                .registerDateTime(LocalDateTime.now())
                .build()));

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));

        given(storeRepository.findByStoreName(anyString())).willReturn(Optional.ofNullable(StoreEntity.builder()
                .storeName("스타벅스1").build()));

        given(listOrderRepository.save(any())).willReturn(ListOrderEntity.builder()
                .pay(Pay.CARD)
                .orderDateTime(LocalDateTime.now())
                .quantity(2)
                .userName(member)
                .orderCompleteDateTime(null)
                .orderStatus(OrderStatus.ING)
                .price(8200)
                .store(StoreEntity.builder().storeId(1).storeName("스타벅스1").build())
                .build());

        //when
        orderService.orderReceipt(order, member.getUsername());

        //then
        assertEquals(1, member.getCoupon().getCount());
        assertEquals(1, member.getPoint().getCount());
    }

    @Test
    @DisplayName("요청 상태의 음료가 없음")
    void checkStatusEmpty() {

        //given
        given(listOrderRepository.findByOrderStatus(OrderStatus.ING, Pageable.ofSize(1)))
                .willReturn(Page.empty());

        //when
        CustomException exception = assertThrows(CustomException.class, () -> orderService.checkStatus(OrderStatus.ING, Pageable.ofSize(1)));

        //then
        assertEquals(NOT_EXIST_ORDER_LIST, exception.getErrorCode());

    }

    @Test
    @DisplayName("음료 상태 변경 요청 실패 - 주문 번호 없음")
    void changeOrderStatusFailNoOrderId() {
        //given
        given(listOrderRepository.findById(anyInt())).willReturn(Optional.empty());

        //when
        CustomException exception = assertThrows(CustomException.class,
                        () -> orderService.changeOrderStatus(1));

        //then
        assertEquals(NOT_EXIST_ORDER_LIST, exception.getErrorCode());
    }

    @Test
    @DisplayName("음료 상태 변경 요청 실패 - 이미 변경 완료")
    void changeOrderStatusFailAlready() {
        //given
        ListOrderEntity listOrder = ListOrderEntity.builder()
                .no(1)
                .userName(MemberEntity.builder().id(1L).username("test1").build())
                .orderStatus(COMPLETE)
                .price(4100)
                .store(StoreEntity.builder().storeId(1).storeName("스타벅스1").build())
                .orderDateTime(LocalDateTime.now())
                .pay(Pay.CARD)
                .quantity(1)
                .menu("아메리카노")
                .orderCompleteDateTime(null)
                .build();

        given(listOrderRepository.findById(anyInt())).willReturn(Optional.of(listOrder));

        //when
        CustomException exception =
                assertThrows(CustomException.class,
                        () -> orderService.changeOrderStatus(1));

        //then
        assertEquals(ALREADY_FINISHED_DRINK, exception.getErrorCode());
    }

    @Test
    @DisplayName("음료 상태 변경 요청 성공")
    void changeOrderStatusSuccess (){
        //given

        ListOrderEntity listOrder = ListOrderEntity.builder()
                .no(1)
                .userName(MemberEntity.builder().id(1L).username("test1").build())
                .orderStatus(OrderStatus.ING)
                .price(4100)
                .store(StoreEntity.builder().storeId(1).storeName("스타벅스1").build())
                .orderDateTime(LocalDateTime.now())
                .pay(Pay.CARD)
                .quantity(1)
                .menu("아메리카노")
                .orderCompleteDateTime(null)
                .build();

        given(listOrderRepository.save(any(ListOrderEntity.class))).willReturn(listOrder);
        given(listOrderRepository.findById(anyInt())).willReturn(Optional.ofNullable(listOrder));

        //when
        var result = orderService.changeOrderStatus(1);

        //then
        assertEquals(COMPLETE, result.getOrderStatus());
    }

    @Test
    @DisplayName("기간별 주문 리스트 보기 - 실패")
    void getOrderListFail() {
        //given
        given(listOrderRepository.findByOrderDateTimeBetween(any(LocalDateTime.class), any(LocalDateTime.class), any())).willReturn(Page.empty());

        //when
        CustomException exception =
                assertThrows(CustomException.class, () -> orderService.getOrderList(LocalDate.parse("2023-03-01"), LocalDate.parse("2023-03-02"), Pageable.ofSize(1)));

        //then
        assertEquals(NOT_EXIST_ORDER_LIST, exception.getErrorCode());

    }

    @Test
    @DisplayName("기간별 주문 리스트 보기 - 성공")
    void getOrderListSuccess() {
        //given
        ListOrderEntity listOrder = ListOrderEntity.builder()
                .no(1)
                .userName(MemberEntity.builder().username("test1").build())
                .orderStatus(OrderStatus.ING)
                .price(4100)
                .store(StoreEntity.builder().storeId(1).storeName("스타벅스1").build())
                .orderDateTime(LocalDateTime.now())
                .pay(Pay.CARD)
                .quantity(1)
                .menu("아메리카노")
                .orderCompleteDateTime(null)
                .build();

        List<ListOrderEntity> list = new ArrayList<>();
        list.add(listOrder);
        Page<ListOrderEntity> page = new PageImpl<>(list);

        given(listOrderRepository.findByOrderDateTimeBetween(any(LocalDateTime.class),
                any(LocalDateTime.class), any())).willReturn(page);

        //when
        var result = orderService.getOrderList(LocalDate.parse("2023-03-01"),
                LocalDate.parse("2023-03-02"), Pageable.ofSize(1));

        //then
        assertEquals(1, result.getSize());

    }

    @Test
    @DisplayName("사용자의 주문 리스트 가져오기 - 없음")
    void getUserOrderListFail() {
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
                .listOrder(new ArrayList<>())
                .build();

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.ofNullable(member));

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> orderService.getUserOrderList("user@naver.com",
                Pageable.ofSize(10)));

        //then
        assertEquals(NOT_EXIST_ORDER_LIST, exception.getErrorCode());
    }

    @Test
    @DisplayName("사용자의 주문 리스트 가져오기 - 성공")
    void getUserOrderListSuccess() {
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

        ListOrderEntity listOrder = ListOrderEntity.builder()
                .no(1)
                .userName(member)
                .orderStatus(OrderStatus.ING)
                .price(4100)
                .store(StoreEntity.builder().storeId(1).storeName("스타벅스1").build())
                .orderDateTime(LocalDateTime.now())
                .pay(Pay.CARD)
                .quantity(1)
                .menu("아메리카노")
                .orderCompleteDateTime(null)
                .build();

        member.setListOrder(List.of(listOrder));

        given(memberRepository.findByUsername(anyString())).willReturn(Optional.of(member));

        //when
        var result = orderService.getUserOrderList(member.getUsername(), Pageable.ofSize(10));

        //then
        assertEquals(1, result.getTotalElements());
    }

    @Test
    @DisplayName("지점의 매출, 주문 리스트 가져오기 - 실패")
    void getOrderListByStoreNameFail() {
        //given
        given(storeRepository.findByStoreName(anyString())).willReturn(
                Optional.ofNullable(
                        StoreEntity.builder()
                                .storeName("스타벅스")
                                .list(new ArrayList<>())
                                .build()));

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> orderService.getOrderListByStoreName("스타벅스",
                        LocalDate.parse("2023-03-01"), LocalDate.parse("2023-03-31"),
                        Pageable.ofSize(10)));

        //then
        assertEquals(NOT_EXIST_STORE_SALES_DATA, exception.getErrorCode());
    }

    @Test
    @DisplayName("지점의 매출, 주문 리스트 가져오기 - 성공")
    void getOrderListByStoreNameSuccess() {
        //given
        ListOrderEntity listOrder = ListOrderEntity.builder()
                .no(1)
                .userName(MemberEntity.builder().username("Test1").build())
                .orderStatus(OrderStatus.ING)
                .price(4100)
                .store(StoreEntity.builder().storeId(1).storeName("스타벅스1").build())
                .orderDateTime(LocalDateTime.now())
                .pay(Pay.CARD)
                .quantity(1)
                .menu("아메리카노")
                .orderCompleteDateTime(null)
                .build();


        given(storeRepository.findByStoreName(anyString())).willReturn(
                Optional.ofNullable(
                        StoreEntity.builder()
                                .storeName("스타벅스1")
                                .list(List.of(listOrder))
                                .build()));

        //when
        var result = orderService.getOrderListByStoreName(listOrder.getStore().getStoreName(),
                LocalDate.parse("2023-05-01"), LocalDate.parse("2023-05-31"),
                Pageable.ofSize(10));

        //then
        assertEquals(4100, result.getSum());
        assertEquals(1, result.getOrderList().getTotalElements());

    }

    @Test
    @DisplayName("전체 지점의 매출 가져오기 - 없음")
    void getEachStoreSalesPriceFail() {
        //given
        Page<StoreEntity> list = new PageImpl<>(List.of());
        given(storeRepository.findAll(any(Pageable.class))).willReturn(list);

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> orderService.getEachStoreSalesPrice(
                        LocalDate.parse("2023-03-01"),
                        LocalDate.parse("2023-03-02"),
                        Pageable.ofSize(10)));
        //then
        assertEquals(NOT_EXIST_STORE_SALES_DATA, exception.getErrorCode());
    }

    @Test
    @DisplayName("전체 지점의 매출 가져오기 - 성공")
    void getEachStoreSalesPriceSuccess() {
        //given
        ListOrderEntity listOrder1 = ListOrderEntity.builder()
                .no(1)
                .userName(MemberEntity.builder().username("test1").build())
                .orderStatus(OrderStatus.ING)
                .price(4100)
                .store(StoreEntity.builder().storeId(1).storeName("스타벅스1").build())
                .orderDateTime(LocalDateTime.now())
                .pay(Pay.CARD)
                .quantity(1)
                .menu("아메리카노")
                .orderCompleteDateTime(null)
                .build();

        ListOrderEntity listOrder2 = ListOrderEntity.builder()
                .no(1)
                .userName(MemberEntity.builder().username("test2").build())
                .orderStatus(OrderStatus.ING)
                .price(4100)
                .store(StoreEntity.builder().storeId(1).storeName("스타벅스2").build())
                .orderDateTime(LocalDateTime.now())
                .pay(Pay.CARD)
                .quantity(1)
                .menu("아메리카노")
                .orderCompleteDateTime(null)
                .build();

        Page<StoreEntity> page = new PageImpl<>(List.of(
                StoreEntity.builder()
                        .storeName("스타벅스1")
                        .list(List.of(listOrder1))
                        .build(),
                StoreEntity.builder()
                        .storeName("스타벅스2")
                        .list(List.of(listOrder2))
                        .build()
                ));

        given(storeRepository.findAll(any(Pageable.class))).willReturn(page);

        //when
        var result = orderService.getEachStoreSalesPrice(
                LocalDate.parse("2023-05-01"), LocalDate.parse("2023-06-02"),
                Pageable.ofSize(10));

        //then
        assertEquals(4100, result.get().toList().get(0).getTotalPrice());
        assertEquals("스타벅스1", result.get().toList().get(0).getStoreName());
        assertEquals(4100, result.get().toList().get(1).getTotalPrice());
        assertEquals("스타벅스2", result.get().toList().get(1).getStoreName());
    }
}