package com.zerobase.order_drinks.repository;


import com.zerobase.order_drinks.model.entity.MemberEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    Optional<MemberEntity> findByUsername(String username);
    boolean existsByUsername(String username);

    Optional<MemberEntity> findByEmailAuthKey(String emailAuthKey);
}
