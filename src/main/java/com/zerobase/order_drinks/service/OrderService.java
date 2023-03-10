package com.zerobase.order_drinks.service;


import com.zerobase.order_drinks.exception.impl.member.LowCardPriceException;
import com.zerobase.order_drinks.exception.impl.member.NoUserException;
import com.zerobase.order_drinks.exception.impl.member.NotUserCouponException;
import com.zerobase.order_drinks.exception.impl.menu.NoMenuException;
import com.zerobase.order_drinks.exception.impl.menu.NotOrderListException;
import com.zerobase.order_drinks.model.constants.OrderStatus;
import com.zerobase.order_drinks.model.constants.Pay;
import com.zerobase.order_drinks.model.dto.Order;
import com.zerobase.order_drinks.model.dto.OrderComplete;
import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import com.zerobase.order_drinks.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final MenuRepository menuRepository;
    private final MemberRepository memberRepository;
    private final ListOrderRepository listOrderRepository;
    private final int pointToCoupon = 12;

    public OrderComplete orderReceipt(Order order, String userName){
        var menu = menuRepository.findByMenuName(order.getItem())
                .orElseThrow(() -> new NoMenuException());
        int price = menu.getPrice() * order.getQuantity();

        var user = memberRepository.findByUsername(userName).orElseThrow(() -> new NoUserException());

        Pay payMethod = order.getPay();

        if(payMethod == Pay.CARD){
            //card 금액 차감
            if(user.getCard().getPrice() < price){
                throw new LowCardPriceException();
            }
            user.getCard().setPrice(user.getCard().getPrice() - price);

            //주문시 포인트 적립 (포인트가 12개가 되면 자동으로 쿠폰으로 교환)
            int updatePoint = user.getPoint().getCount() + order.getQuantity();
            if(updatePoint == pointToCoupon){
                user.getCoupon().setCount(user.getCoupon().getCount() + 1);
                user.getPoint().setCount(user.getPoint().getCount() - pointToCoupon);
            }
            else{
                user.getPoint().setCount(updatePoint);
            }
        }
        else if(payMethod == Pay.COUPON){
            if(user.getCoupon().getCount() < 1){
                throw new NotUserCouponException();
            }

            user.getCoupon().setCount(user.getCoupon().getCount() - 1);
        }

        memberRepository.save(user);
        var result = listOrderRepository.save(order.toEntity(price, userName));

        return new OrderComplete().toDto(result, order.getPay());
    }

    public List<ListOrderEntity> checkList(OrderStatus status) {

        var orderList = listOrderRepository.findByOrderStatus(status);
        if(orderList.size() == 0){
            throw new NotOrderListException();
        }
        return orderList;
    }

    public ListOrderEntity changeOrderStatus(int orderNo) {
        var orderStatus = listOrderRepository.findById(orderNo)
                .orElseThrow(() -> new NotOrderListException());

        if(orderStatus.getOrderStatus() == OrderStatus.COMPLETE){
            throw new RuntimeException("이미 주문 완료된 음료 입니다.");
        }

        orderStatus.setOrderStatus(OrderStatus.COMPLETE);
        orderStatus.setOrderCompleteDateTime(LocalDateTime.now());

        return listOrderRepository.save(orderStatus);
    }

    public List<ListOrderEntity> getOrderList(LocalDate start, LocalDate end) {
        var result = listOrderRepository.findByOrderDateTimeBetween(start.atTime(0, 0,0), end.atTime(23, 59,59));
        if(result.size() == 0){
            throw new NotOrderListException();
        }
        return result;
    }
}
