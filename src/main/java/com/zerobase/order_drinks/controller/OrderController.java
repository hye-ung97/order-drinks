package com.zerobase.order_drinks.controller;

import com.zerobase.order_drinks.model.constants.OrderStatus;
import com.zerobase.order_drinks.model.dto.Order;
import com.zerobase.order_drinks.model.dto.StoreData;
import com.zerobase.order_drinks.notification.NotificationService;
import com.zerobase.order_drinks.service.GoogleMapService;
import com.zerobase.order_drinks.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final GoogleMapService googleMapService;
    private final NotificationService notificationService;

    @GetMapping("/find-store")
    public ResponseEntity<?> findLocation(@RequestParam("address") String address) {
        List<StoreData> storeData = googleMapService.findStoreFromApi(address);
        return ResponseEntity.ok(storeData);
    }

    @PostMapping() // 음료 주문
    public ResponseEntity<?> order(@RequestBody Order order, @AuthenticationPrincipal UserDetails user){
        var result = orderService.orderReceipt(order, user.getUsername());
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list/{status}") //제작중 / 제작완료 음료 리스트 보기
    public ResponseEntity<?> orderStatus(@PathVariable OrderStatus status){
        var result = orderService.checkStatus(status);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status-change") //음료 상태 변경
    public ResponseEntity<?> orderStatusChange(@RequestParam int orderNo){
        var result = orderService.changeOrderStatus(orderNo);
        notificationService.send(result.getUserName(), "Your drink is ready!! Pick up please :)", orderNo);
        return ResponseEntity.ok(result);
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list") //기간별 주문 리스트 보기
    public ResponseEntity<?> orderListByTerm(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate){

        var result = orderService.getOrderList(startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list-store") //지점 주문 리스트 보기(기간별)
    public ResponseEntity<?> orderListByStore(@RequestParam String storeName,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate){
        var result = orderService.getOrderListByStoreName(storeName, startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list-each") //각각의 지점별 판매 금액 보기(기간별)
    public ResponseEntity<?> eachStorePriceList(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate){
        var result = orderService.getEachStoreSalesPrice(startDate, endDate);
        return ResponseEntity.ok(result);
    }

}
