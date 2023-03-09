package com.zerobase.order_drinks.service;

import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import com.zerobase.order_drinks.model.entity.MemberEntity;
import com.zerobase.order_drinks.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class AdminService {


    private final MemberRepository memberRepository;

    public Page<MemberEntity> getMemberList(Pageable pageable) {
        return this.memberRepository.findAll(pageable);
    }

    //주문 리스트 가져오기
    public Page<ListOrderEntity> getOrderList(Pageable pageable){
        return null;
    }
}
