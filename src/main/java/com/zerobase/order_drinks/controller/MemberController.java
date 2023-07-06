package com.zerobase.order_drinks.controller;

import com.zerobase.order_drinks.exception.ErrorResponse;
import com.zerobase.order_drinks.model.dto.Auth;
import com.zerobase.order_drinks.model.dto.OrderBillDto;
import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import com.zerobase.order_drinks.model.entity.Wallet;
import com.zerobase.order_drinks.service.MemberService;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final OrderService orderService;

    @Operation(summary = "회원탈퇴", description = "회원탈퇴")
    @GetMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody Auth.SignIn signIn){
        return ResponseEntity.ok(memberService.withdraw(signIn));
    }

    @Operation(summary = "지갑 보기", description = "사용자의 card, coupon, point 확인")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = Wallet.class))),
            @ApiResponse(responseCode = "NOT_EXIST_USER",
                    description = "해당 사용자가 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/wallets")
    public ResponseEntity<Wallet> getWallet(@AuthenticationPrincipal UserDetails user){
        return ResponseEntity.ok(memberService.getWallet(user.getUsername()));
    }

    @Operation(summary = "카드 충전", description = "사용자의 카드 금액 충전")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = ListOrderEntity.class))),
            @ApiResponse(responseCode = "NOT_EXIST_USER",
                    description = "해당 사용자가 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @Parameter(name = "price", description = "충전 금액", example = "1000")
    @PutMapping("/card")
    public ResponseEntity<Wallet.Card> charge(@RequestParam("price") int price, @AuthenticationPrincipal UserDetails user){
        return ResponseEntity.ok(memberService.cardCharge(price, user.getUsername()));
    }

    @Operation(summary = "사용자의 주문 리스트", description = "사용자의 주문 리스트(주문 히스토리)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = OrderBillDto.class))),
            @ApiResponse(responseCode = "NOT_EXIST_ORDER_LIST",
                    description = "주문 리스트가 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/order-list")
    public ResponseEntity<Page<OrderBillDto>> getUserOrder(@AuthenticationPrincipal UserDetails user, Pageable pageable){
        return ResponseEntity.ok(orderService.getUserOrderList(user.getUsername(), pageable));
    }

}
