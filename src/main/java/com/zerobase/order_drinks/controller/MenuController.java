package com.zerobase.order_drinks.controller;

import com.zerobase.order_drinks.exception.ErrorResponse;
import com.zerobase.order_drinks.model.dto.Menu;
import com.zerobase.order_drinks.model.dto.MenuInventory;
import com.zerobase.order_drinks.model.entity.MenuEntity;
import com.zerobase.order_drinks.service.MenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
@RequestMapping("/menus")
public class MenuController {

    private final MenuService menuService;

    @Operation(summary = "메뉴 등록", description = "메뉴 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "메뉴 등록 성공",
                    content = @Content(schema = @Schema(implementation = MenuEntity.class))),
            @ApiResponse(responseCode = "EXIST_MENU",
                    description = "이미 존재하는 메뉴입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Menu> menuRegister(@RequestBody Menu request){
        menuService.menuRegister(request);
        log.info("menu register -> " + request.getMenuName());

        return ResponseEntity.ok(request);
    }

    @Operation(summary = "메뉴 리스트", description = "메뉴 리스트")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = Menu.class))),
            @ApiResponse(responseCode = "NOT_EXIST_MENU_LIST",
                    description = "메뉴 리스트가 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/lists")
    public ResponseEntity<Page<Menu>> menuList(final Pageable pageable){
        return ResponseEntity.ok(menuService.menuList(pageable));
    }

    @Operation(summary = "재고 관리", description = "재고 수정 가능(추가)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = MenuInventory.class))),
            @ApiResponse(responseCode = "NOT_EXIST_MENU",
                    description = "해당 메뉴가 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/inventory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MenuInventory> inventoryManagement(@RequestBody MenuInventory menuInventory){
        return ResponseEntity.ok(menuService.setInventory(menuInventory));
    }

    @Operation(summary = "재고 리스트", description = "재고 리스트")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = MenuInventory.class))),
            @ApiResponse(responseCode = "NOT_EXIST_MENU_LIST",
                    description = "메뉴가 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/inventory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<MenuInventory>> inventoryList (Pageable pageable){
        return ResponseEntity.ok(menuService.getInventory(pageable));
    }
}
