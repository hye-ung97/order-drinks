package com.zerobase.order_drinks.controller;

import com.zerobase.order_drinks.model.entity.MemberEntity;
import com.zerobase.order_drinks.service.AdminService;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @Operation(summary = "회원리스트 보기", description = "회원리스트 보기")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK",
                    content = @Content(schema = @Schema(implementation = MemberEntity.class)))
    })
    @GetMapping("/member/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> memberList(final Pageable pageable){
        Page<MemberEntity> members = this.adminService.getMemberList(pageable);
        return ResponseEntity.ok(members);
    }

}
