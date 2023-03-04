package com.zerobase.order_drinks.controller;

import com.zerobase.order_drinks.model.MenuEntity;
import com.zerobase.order_drinks.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final OrderService orderService;

    @GetMapping("/menu/list")
    public ResponseEntity<?> menuList(final Pageable pageable){
        Page<MenuEntity> menuList = this.orderService.menuList(pageable);
        return ResponseEntity.ok(menuList);
    }

    @GetMapping("/find-store")
    public void findLocation(@RequestParam("address") String address){
        orderService.getLocationData(address);
    }






}
