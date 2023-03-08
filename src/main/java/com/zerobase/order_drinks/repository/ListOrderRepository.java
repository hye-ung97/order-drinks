package com.zerobase.order_drinks.repository;

import com.zerobase.order_drinks.model.ListOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListOrderRepository extends JpaRepository<ListOrderEntity, Integer> {
}
