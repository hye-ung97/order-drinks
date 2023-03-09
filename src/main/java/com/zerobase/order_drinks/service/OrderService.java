package com.zerobase.order_drinks.service;

import com.zerobase.order_drinks.exception.impl.NoMenuException;
import com.zerobase.order_drinks.exception.impl.member.LowCardPriceException;
import com.zerobase.order_drinks.exception.impl.member.NoUserException;
import com.zerobase.order_drinks.model.constants.Pay;
import com.zerobase.order_drinks.model.dto.Order;
import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import com.zerobase.order_drinks.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final MenuRepository menuRepository;
    private final MemberRepository memberRepository;
    private final ListOrderRepository listOrderRepository;
    private final CardRepository cardRepository;
    private final CouponRepository couponRepository;
    private final PointRepository pointRepository;

    private final int pointToCoupon = 12;

    public ListOrderEntity orderReceipt(Order order, String userName){
        var menu = menuRepository.findByMenuName(order.getItem())
                .orElseThrow(() -> new NoMenuException());
        int price = menu.getPrice();

        var user = memberRepository.findByUsername(userName).orElseThrow(() -> new NoUserException());

        Pay payMethod = order.getPay();

        if(payMethod == Pay.CARD){
            if(user.getCard().getPrice() < price){
                throw new LowCardPriceException();
            }
            user.getCard().setPrice(user.getCard().getPrice() - price);

            int updatePoint = user.getPoint().getCount() + 1;
            if(updatePoint == pointToCoupon){
                user.getCoupon().setCount(user.getCoupon().getCount() + 1);
                user.getPoint().setCount(0);
            }
            else{
                user.getPoint().setCount(updatePoint);
            }
        }
        else if(payMethod == Pay.COUPON){
            if(user.getCoupon().getCount() < 1){
                throw new LowCardPriceException();
            }

            user.getCoupon().setCount(user.getCoupon().getCount() - 1);
        }

        memberRepository.save(user);
        return listOrderRepository.save(order.toEntity(price, userName));
    }

}
