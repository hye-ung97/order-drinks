package com.zerobase.order_drinks.controller;

import com.zerobase.order_drinks.exception.ErrorResponse;
import com.zerobase.order_drinks.model.constants.OrderStatus;
import com.zerobase.order_drinks.model.dto.ListOrderDto;
import com.zerobase.order_drinks.model.dto.Order;
import com.zerobase.order_drinks.model.dto.OrderBillDto;
import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import com.zerobase.order_drinks.notification.NotificationService;
import com.zerobase.order_drinks.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final NotificationService notificationService;

    @Operation(summary = "음료 주문", description = "음료 주문")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "주문 완료",
                    content = @Content(schema = @Schema(implementation = OrderBillDto.class))),
            @ApiResponse(responseCode = "NOT_EXIST_MENU", description = "해당 메뉴가 없어 주문 불가합니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "NOT_FOUND_STORE_DATA", description = "지점 정보를 찾을 수 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "OUT_OF_STOCK", description = "해당 메뉴의 재고가 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "LOW_CARD_PRICE", description = "카드 금액이 부족합니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "UNAVAILABLE_COUPON", description = "사용가능한 쿠폰이 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),

    })
    @PostMapping()
    public ResponseEntity<OrderBillDto> order(@RequestBody Order order, @AuthenticationPrincipal UserDetails user){
        return ResponseEntity.ok(orderService.orderReceipt(order, user.getUsername()));
    }

    @Operation(summary = "주문 상태별 리스트", description = "주문 상태별(ING / COMPLETE) 리스트")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ListOrderEntity.class))),
            @ApiResponse(responseCode = "NOT_EXIST_ORDER_LIST",
                    description = "해당 주문 리스트가 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Parameter(name = "status", description = "주문상태")
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list/{status}")
    public ResponseEntity<Page<ListOrderDto>> orderStatus(@PathVariable OrderStatus status, Pageable pageable){
        return ResponseEntity.ok(orderService.checkStatus(status, pageable));
    }

    @Operation(summary = "음료 상태 변경", description = "입력된 주문 번호를 통하여 음료 상태 변경 + 주문자에게 주문 완료 알림")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 완료",
                    content = @Content(schema = @Schema(implementation = ListOrderEntity.class))),
            @ApiResponse(responseCode = "NOT_EXIST_ORDER_LIST",
                    description = "해당 주문 리스트가 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "ALREADY_FINISHED_DRINK",
                    description = "이미 제작 완료된 주문 입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Parameter(name = "orderNo", description = "주문 번호", example = "1")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/status-change") //음료 상태 변경
    public ResponseEntity<ListOrderDto> orderStatusChange(@RequestParam int orderNo){
        ListOrderDto result = orderService.changeOrderStatus(orderNo);
        notificationService.send(result.getUserName(), "Your drink is ready!! Pick up please :)", orderNo);
        return ResponseEntity.ok(result);
    }

}
