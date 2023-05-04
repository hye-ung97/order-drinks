package com.zerobase.order_drinks.repository;

import com.zerobase.order_drinks.model.entity.StoreEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StoreRepository extends JpaRepository<StoreEntity, Integer> {
    boolean existsByStoreName(String storeName);
    Optional<StoreEntity> findByStoreName(String storeName);
}
