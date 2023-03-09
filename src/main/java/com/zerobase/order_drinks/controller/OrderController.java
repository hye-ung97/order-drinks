package com.zerobase.order_drinks.controller;

import com.zerobase.order_drinks.model.dto.Order;
import com.zerobase.order_drinks.model.dto.StoreData;
import com.zerobase.order_drinks.security.TokenProvider;
import com.zerobase.order_drinks.service.GoogleMapService;
import com.zerobase.order_drinks.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final GoogleMapService googleMapService;
    private final TokenProvider tokenProvider;

    @GetMapping("/find-store")
    public ResponseEntity<?> findLocation(@RequestParam("address") String address) {
        List<StoreData> storeData = googleMapService.findStoreFromApi(address);
        return ResponseEntity.ok(storeData);
    }

    @PostMapping() // 음료 주문
    public ResponseEntity<?> order(@RequestBody Order order, @RequestHeader("Authorization") String token){
        String userName = this.tokenProvider.getUsername(token.replace("Bearer ", ""));
        var result = orderService.orderReceipt(order, userName);

        return ResponseEntity.ok(result);
    }

}
