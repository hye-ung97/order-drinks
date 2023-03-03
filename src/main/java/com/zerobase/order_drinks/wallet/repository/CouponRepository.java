package com.zerobase.order_drinks.wallet.repository;


import com.zerobase.order_drinks.wallet.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponRepository extends JpaRepository <Coupon, Integer> {
}
