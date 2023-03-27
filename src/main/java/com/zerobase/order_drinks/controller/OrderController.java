package com.zerobase.order_drinks.controller;

import com.zerobase.order_drinks.exception.ErrorResponse;
import com.zerobase.order_drinks.model.constants.OrderStatus;
import com.zerobase.order_drinks.model.dto.*;
import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import com.zerobase.order_drinks.notification.NotificationService;
import com.zerobase.order_drinks.service.GoogleMapService;
import com.zerobase.order_drinks.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
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

    @Operation(summary = "지점 찾기", description = "입력한 주소 기준으로 가까운 지점 찾기")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = StoreData.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST")
    })
    @Parameter(name = "address", description = "현재 주소 입력", example = "서울시 강남구 테헤란로 231")
    @GetMapping("/find-store")
        public ResponseEntity<?> findLocation(@RequestParam("address") String address) {
        List<StoreData> storeData = googleMapService.findStoreFromApi(address);
        return ResponseEntity.ok(storeData);
    }

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
    public ResponseEntity<?> order(@RequestBody Order order, @AuthenticationPrincipal UserDetails user){
        var result = orderService.orderReceipt(order, user.getUsername());
        return ResponseEntity.ok(result);
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
    public ResponseEntity<?> orderStatus(@PathVariable OrderStatus status, Pageable pageable){
        var result = orderService.checkStatus(status, pageable);
        return ResponseEntity.ok(result);
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
    public ResponseEntity<?> orderStatusChange(@RequestParam int orderNo){
        var result = orderService.changeOrderStatus(orderNo);
        notificationService.send(result.getUserName(), "Your drink is ready!! Pick up please :)", orderNo);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "기간별 주문 리스트 보기", description = "모든 지점의 기간별 주문 리스트 보기")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = ListOrderEntity.class))),
            @ApiResponse(responseCode = "NOT_EXIST_ORDER_LIST",
                    description = "해당 주문 리스트가 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Parameters({
            @Parameter(name = "startDate", description = "날짜", example = "2023-01-01"),
            @Parameter(name = "endDate", description = "날짜", example = "2023-01-31")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list")
    public ResponseEntity<?> orderListByTerm(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                             Pageable pageable){

        var result = orderService.getOrderList(startDate, endDate, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "특정 지점의 기간별 주문 리스트 보기", description = "특정 지점의 기간별 주문 리스트 및 매출액 보기")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = StoreOrderBillDto.class))),
            @ApiResponse(responseCode = "NOT_FOUND_STORE_DATA",
                    description = "해당 지점이 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Parameters({
            @Parameter(name = "storeName", description = "지점명", example = "스타벅스 노량진점"),
            @Parameter(name = "startDate", description = "날짜", example = "2023-01-01"),
            @Parameter(name = "endDate", description = "날짜", example = "2023-01-31")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list-store") //지점 주문 리스트 보기(기간별)
    public ResponseEntity<?> orderListByStore(@RequestParam String storeName,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                              Pageable pageable){
        var result = orderService.getOrderListByStoreName(storeName, startDate, endDate, pageable);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "각각의 지점별 판매 금액 보기(기간별)", description = "각각의 지점별 판매 금액 보기(기간별)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = StoreGroupDto.class))),
            @ApiResponse(responseCode = "NOT_EXIST_STORE_SALES_DATA",
                    description = "요청하신 지점별 매출 리스트가 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Parameters({
            @Parameter(name = "startDate", description = "날짜", example = "2023-01-01"),
            @Parameter(name = "endDate", description = "날짜", example = "2023-01-31")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/list-each")
    public ResponseEntity<?> eachStorePriceList(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                Pageable pageable){
        var result = orderService.getEachStoreSalesPrice(startDate, endDate, pageable);
        return ResponseEntity.ok(result);
    }

}
