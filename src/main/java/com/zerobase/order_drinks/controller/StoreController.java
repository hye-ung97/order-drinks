package com.zerobase.order_drinks.controller;

import com.zerobase.order_drinks.exception.ErrorResponse;
import com.zerobase.order_drinks.model.dto.ListOrderDto;
import com.zerobase.order_drinks.model.dto.StoreData;
import com.zerobase.order_drinks.model.dto.StoreGroup;
import com.zerobase.order_drinks.model.dto.StoreOrderBillDto;
import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import com.zerobase.order_drinks.service.GoogleMapService;
import com.zerobase.order_drinks.service.StoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/stores")
@RequiredArgsConstructor
public class StoreController {
    private final StoreService storeService;
    private final GoogleMapService googleMapService;

    @Operation(summary = "지점 찾기", description = "입력한 주소 기준으로 가까운 지점 찾기")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = StoreData.class))),
            @ApiResponse(responseCode = "400", description = "BAD REQUEST")
    })
    @Parameter(name = "address", description = "현재 주소 입력", example = "서울시 강남구 테헤란로 231")
    @GetMapping("/find-store")
        public ResponseEntity<List<StoreData>> findLocation(@RequestParam("address") String address) {
        return ResponseEntity.ok(googleMapService.findStoreFromApi(address));
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
    @GetMapping("/sales/total/lists")
    public ResponseEntity<Page<ListOrderDto>> orderListByTerm(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                             Pageable pageable){

        return ResponseEntity.ok(storeService.getOrderList(startDate, endDate, pageable));
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
    @GetMapping("/sales/each/list") //지점 주문 리스트 보기(기간별)
    public ResponseEntity<StoreOrderBillDto> orderListByStore(
            @RequestParam String storeName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable){

        return ResponseEntity.ok(storeService.getOrderListByStoreName(
                storeName, startDate, endDate, pageable));
    }

    @Operation(summary = "각각의 지점별 판매 금액 보기(기간별)", description = "각각의 지점별 판매 금액 보기(기간별)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = StoreGroup.class))),
            @ApiResponse(responseCode = "NOT_EXIST_STORE_SALES_DATA",
                    description = "요청하신 지점별 매출 리스트가 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Parameters({
            @Parameter(name = "startDate", description = "날짜", example = "2023-01-01"),
            @Parameter(name = "endDate", description = "날짜", example = "2023-01-31")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/sales/each/total-list")
    public ResponseEntity<Page<StoreGroup>> eachStorePriceList(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Pageable pageable){

        return ResponseEntity.ok(storeService.getEachStoreSalesPrice(startDate, endDate, pageable));
    }

}
