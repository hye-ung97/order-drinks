package com.zerobase.order_drinks.controller;

import antlr.Token;
import com.zerobase.order_drinks.model.dto.Menu;
import com.zerobase.order_drinks.security.TokenProvider;
import com.zerobase.order_drinks.service.MenuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/menu")
public class MenuController {

    private final MenuService menuService;
    private final TokenProvider tokenProvider;

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> menuRegister(@RequestBody Menu request){
        var result = menuService.menuRegister(request);
        log.info("menu register -> " + request.getMenuName());

        return ResponseEntity.ok(request);
    }

    @GetMapping("/list")
    public ResponseEntity<?> menuList(final Pageable pageable){
        Page<Menu> menuList = this.menuService.menuList(pageable);
        return ResponseEntity.ok(menuList);
    }
}
