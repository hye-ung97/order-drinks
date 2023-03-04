package com.zerobase.order_drinks.service;

import com.zerobase.order_drinks.exception.impl.AlreadyExistMenuException;
import com.zerobase.order_drinks.exception.impl.member.AlreadyExistUserException;
import com.zerobase.order_drinks.model.MemberEntity;
import com.zerobase.order_drinks.model.Menu;
import com.zerobase.order_drinks.model.MenuEntity;
import com.zerobase.order_drinks.repository.MemberRepository;
import com.zerobase.order_drinks.repository.MenuRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AdminService {

    private final MenuRepository menuRepository;
    private final MemberRepository memberRepository;


    public MenuEntity menuRegister(Menu menu){
        boolean exists = this.menuRepository.existsByMenuName(menu.getMenuName());
        if(exists){
            throw new AlreadyExistMenuException();
        }

        var result = this.menuRepository.save(menu.toEntity());

        return result;
    }


    public Page<MemberEntity> getMemberList(Pageable pageable) {
        return this.memberRepository.findAll(pageable);
    }
}
