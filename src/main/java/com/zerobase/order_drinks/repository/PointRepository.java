package com.zerobase.order_drinks.repository;

import com.zerobase.order_drinks.model.entity.WalletEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointRepository extends JpaRepository<WalletEntity.Point, Integer> {
}
