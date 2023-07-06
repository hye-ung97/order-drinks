package com.zerobase.order_drinks.service;


import com.zerobase.order_drinks.exception.CustomException;
import com.zerobase.order_drinks.model.constants.OrderStatus;
import com.zerobase.order_drinks.model.constants.Pay;
import com.zerobase.order_drinks.model.dto.ListOrderDto;
import com.zerobase.order_drinks.model.dto.Order;
import com.zerobase.order_drinks.model.dto.OrderBillDto;
import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import com.zerobase.order_drinks.model.entity.MemberEntity;
import com.zerobase.order_drinks.model.entity.MenuEntity;
import com.zerobase.order_drinks.model.entity.StoreEntity;
import com.zerobase.order_drinks.notification.NotificationService;
import com.zerobase.order_drinks.repository.ListOrderRepository;
import com.zerobase.order_drinks.repository.MemberRepository;
import com.zerobase.order_drinks.repository.MenuRepository;
import com.zerobase.order_drinks.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.zerobase.order_drinks.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final MenuRepository menuRepository;
    private final MemberRepository memberRepository;
    private final ListOrderRepository listOrderRepository;
    private final StoreRepository storeRepository;
    private final NotificationService notificationService;

    private final int pointToCoupon = 12;

    private MemberEntity getUser(String userName) {
        return memberRepository.findByUsername(userName)
                .orElseThrow(() -> new CustomException(NOT_EXIST_USER));
    }

    private StoreEntity getStore(String storeName) {
        return storeRepository.findByStoreName(storeName)
                .orElseThrow(() -> new CustomException(NOT_FOUND_STORE_DATA));
    }

    public OrderBillDto orderReceipt(Order order, String userName){
        MenuEntity menu = menuRepository.findByMenuName(order.getItem())
                .orElseThrow(() -> new CustomException(NOT_EXIST_MENU));

        StoreEntity store = getStore(order.getStoreName());

        if(menu.getQuantity() < order.getQuantity()){
            outOfStockAlert(menu);
            throw new CustomException(OUT_OF_STOCK);
        }

        int price = menu.getPrice() * order.getQuantity();

        MemberEntity user = getUser(userName);

        Pay payMethod = order.getPay();

        if(payMethod == Pay.CARD){
            //card 금액 차감
            if(user.getCard().getPrice() < price){
                throw new CustomException(LOW_CARD_PRICE);
            }
            user.getCard().setPrice(user.getCard().getPrice() - price);

            //주문시 포인트 적립 (포인트가 12개가 되면 자동으로 쿠폰으로 교환)
            int updatePoint = user.getPoint().getCount() + order.getQuantity();
            if(updatePoint >= pointToCoupon){
                user.getCoupon().setCount(user.getCoupon().getCount() + 1);
                user.getPoint().setCount(updatePoint - pointToCoupon);
            }
            else{
                user.getPoint().setCount(updatePoint);
            }
        }
        else if(payMethod == Pay.COUPON){
            if(user.getCoupon().getCount() < 1){
                throw new CustomException(UNAVAILABLE_COUPON);
            }

            user.getCoupon().setCount(user.getCoupon().getCount() - 1);
        }

        ListOrderEntity result = listOrderRepository.save(order.toEntity(price, user, store));
        List<ListOrderEntity> listOrder = user.getListOrder();
        listOrder.add(result);
        user.setListOrder(listOrder);

        memberRepository.save(user);
        menu.setQuantity(menu.getQuantity() - order.getQuantity());
        menuRepository.save(menu);
        return new OrderBillDto().toDto(result);
    }

    //모든 admin 에게 알림
    public void outOfStockAlert(MenuEntity menu){
        List<MemberEntity> list = memberRepository.findAll();
        List<MemberEntity> result = list.stream().filter(
                n -> n.getRoles().contains("ROLE_ADMIN")).toList();
        result.stream().map(MemberEntity::getUsername).forEach(receiver ->
                notificationService.send(receiver,
                        menu.getMenuName() + " 재고가 부족합니다.", menu.getQuantity()));
    }

    public Page<ListOrderDto> checkStatus(OrderStatus status, Pageable pageable) {

        Page<ListOrderEntity> listOrderEntityPage =
                listOrderRepository.findByOrderStatus(status, pageable);
        if(listOrderEntityPage.isEmpty()){
            throw new CustomException(NOT_EXIST_ORDER_LIST);
        }
        return listOrderEntityPage.map(m -> new ListOrderDto().toDto(m));
    }

    public ListOrderDto changeOrderStatus(int orderNo) {
        ListOrderEntity orderStatus = listOrderRepository.findById(orderNo)
                .orElseThrow(() -> new CustomException(NOT_EXIST_ORDER_LIST));

        if(orderStatus.getOrderStatus() == OrderStatus.COMPLETE){
            throw new CustomException(ALREADY_FINISHED_DRINK);
        }

        orderStatus.setOrderStatus(OrderStatus.COMPLETE);
        orderStatus.setOrderCompleteDateTime(LocalDateTime.now());

        return new ListOrderDto().toDto(listOrderRepository.save(orderStatus));
    }

    public Page<OrderBillDto> getUserOrderList(String userName, Pageable pageable) {
        MemberEntity user = getUser(userName);
        if(user.getListOrder().isEmpty()){
            throw new CustomException(NOT_EXIST_ORDER_LIST);
        }

        List<OrderBillDto> orderBillDto = user.getListOrder().stream()
                .map(m -> new OrderBillDto().toDto(m)).toList();

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), orderBillDto.size());

        return new PageImpl<>(orderBillDto.subList(start, end), pageable, orderBillDto.size());
    }
}
