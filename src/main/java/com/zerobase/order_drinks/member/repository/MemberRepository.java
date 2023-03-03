package com.zerobase.order_drinks.member.repository;


import com.zerobase.order_drinks.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Integer> {
    boolean existsByUserId(String userId);
    Optional<Member> findByEmailAuthKey(String emailAuthKey);

    Optional<Member> findByUserId(String userId);

}
