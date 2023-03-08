package com.zerobase.order_drinks.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.zerobase.order_drinks.components.GoogleMapApi;
import com.zerobase.order_drinks.exception.impl.NoMenuException;
import com.zerobase.order_drinks.model.constants.Pay;
import com.zerobase.order_drinks.model.dto.Order;
import com.zerobase.order_drinks.model.dto.StoreData;
import com.zerobase.order_drinks.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {
    private final MenuRepository menuRepository;
    private final ListOrderRepository listOrderRepository;
    private final GoogleMapApi googleMapApi;
    private final CardRepository cardRepository;
    private final CouponRepository couponRepository;
    private final PointRepository pointRepository;


    public List<StoreData> getLocationData(String address) throws JsonProcessingException {
        return this.googleMapApi.findStoreFromApi(address);
    }

    public void orderReceipt(Order order){
        var menu = menuRepository.findByMenuName(order.getItem())
                .orElseThrow(() -> new NoMenuException());

        int price = menu.getPrice();

       Pay payMethod = order.getPay();

        if(payMethod == Pay.CARD){

        }
        else if(payMethod == Pay.COUPON){

        }

    }

}
