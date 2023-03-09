package com.zerobase.order_drinks.controller;

import com.zerobase.order_drinks.model.constants.OrderStatus;
import com.zerobase.order_drinks.model.dto.Order;
import com.zerobase.order_drinks.model.dto.StoreData;
import com.zerobase.order_drinks.security.TokenProvider;
import com.zerobase.order_drinks.service.GoogleMapService;
import com.zerobase.order_drinks.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list/{status}") //제작중 / 제작완료 음료 리스트 보기
    public ResponseEntity<?> orderStatus(@PathVariable OrderStatus status){
        var result = orderService.checkList(status);
        return ResponseEntity.ok(result);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/status-change") //음료 상태 변경
    public ResponseEntity<?> orderStatusChange(@RequestParam int orderNo){
        var result = orderService.changeOrderStatus(orderNo);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/list") //기간별 주문 리스트 보기
    public ResponseEntity<?> orderList(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate){

        var result = orderService.getOrderList(startDate, endDate);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/list-store") //지점 주문 리스트 보기
    public ResponseEntity<?> orderStoreList(){
        return null;
    }

}
