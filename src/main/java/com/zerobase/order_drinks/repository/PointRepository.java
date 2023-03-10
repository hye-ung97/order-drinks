package com.zerobase.order_drinks.repository;

import com.zerobase.order_drinks.model.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<Wallet.Point, Integer> {
}
