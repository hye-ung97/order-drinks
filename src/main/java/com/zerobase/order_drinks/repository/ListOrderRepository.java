package com.zerobase.order_drinks.repository;

import com.zerobase.order_drinks.model.constants.OrderStatus;
import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface ListOrderRepository extends JpaRepository<ListOrderEntity, Integer> {

    Page<ListOrderEntity> findByOrderStatus(OrderStatus status, Pageable pageable);

    Page<ListOrderEntity> findByOrderDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);
}
