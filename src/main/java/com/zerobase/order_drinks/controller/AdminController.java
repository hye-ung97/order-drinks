package com.zerobase.order_drinks.controller;

import com.zerobase.order_drinks.model.MemberEntity;
import com.zerobase.order_drinks.model.Menu;
import com.zerobase.order_drinks.service.AdminService;
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
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;

    @PostMapping("/menu/register")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> menuRegister(@RequestBody Menu request){
        var result = adminService.menuRegister(request);
        log.info("menu register -> " + request.getMenuName());

        return ResponseEntity.ok(result);
    }

    @GetMapping("/member/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> memberList(final Pageable pageable){
        Page<MemberEntity> members = this.adminService.getMemberList(pageable);
        return ResponseEntity.ok(members);
    }

}
