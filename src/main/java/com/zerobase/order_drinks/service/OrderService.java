package com.zerobase.order_drinks.service;


import com.zerobase.order_drinks.exception.impl.AlreadyFinishedDrinkException;
import com.zerobase.order_drinks.exception.impl.NotFoundStoreDataException;
import com.zerobase.order_drinks.exception.impl.member.LowCardPriceException;
import com.zerobase.order_drinks.exception.impl.member.NoUserException;
import com.zerobase.order_drinks.exception.impl.member.NotUserCouponException;
import com.zerobase.order_drinks.exception.impl.menu.NoMenuException;
import com.zerobase.order_drinks.exception.impl.menu.NotOrderListException;
import com.zerobase.order_drinks.model.constants.OrderStatus;
import com.zerobase.order_drinks.model.constants.Pay;
import com.zerobase.order_drinks.model.dto.Order;
import com.zerobase.order_drinks.model.dto.OrderBillDto;
import com.zerobase.order_drinks.model.dto.StoreGroupDtoImp;
import com.zerobase.order_drinks.model.dto.StoreOrderBillDto;
import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import com.zerobase.order_drinks.repository.ListOrderRepository;
import com.zerobase.order_drinks.repository.MemberRepository;
import com.zerobase.order_drinks.repository.MenuRepository;
import com.zerobase.order_drinks.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final StoreRepository storeRepository;

    private final int pointToCoupon = 12;

    public OrderBillDto orderReceipt(Order order, String userName){
        var menu = menuRepository.findByMenuName(order.getItem())
                .orElseThrow(() -> new NoMenuException());

        if(!storeRepository.existsByStoreName(order.getStoreName())){
            throw new NotFoundStoreDataException();
        }

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
            if(updatePoint >= pointToCoupon){
                user.getCoupon().setCount(user.getCoupon().getCount() + 1);
                user.getPoint().setCount(user.getPoint().getCount() - pointToCoupon + 1);
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

        return new OrderBillDto().toDto(result);
    }

    public List<ListOrderEntity> checkStatus(OrderStatus status) {

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
            throw new AlreadyFinishedDrinkException();
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

    public Page<OrderBillDto> getUserOrderList(String userName, Pageable pageable) {
        Page<ListOrderEntity> listOrderEntityPage = listOrderRepository.findByUserName(userName, pageable);
        Page<OrderBillDto> orderBillDto = listOrderEntityPage.map(m -> new OrderBillDto().toDto(m));
        return orderBillDto;
    }

    public StoreOrderBillDto getOrderListByStoreName(String storeName, LocalDate startDate, LocalDate endDate) {
        var result = listOrderRepository.findByStoreAndOrderDateTimeBetween(storeName,
                startDate.atTime(0,0,0), endDate.atTime(23,59,59));
        long sum = result.stream().mapToLong(ListOrderEntity::getPrice).sum();

        StoreOrderBillDto storeOrderBillDto = new StoreOrderBillDto();
        storeOrderBillDto.setSum(sum);
        storeOrderBillDto.setOrderList(result);
        return storeOrderBillDto;
    }

    public List<StoreGroupDtoImp> getEachStoreSalesPrice(LocalDate startDate, LocalDate endDate){
        return listOrderRepository.findByStoreGroupSalesPrice(startDate, endDate);
    }
}
