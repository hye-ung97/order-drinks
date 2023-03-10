package com.zerobase.order_drinks.repository;

import com.zerobase.order_drinks.model.constants.OrderStatus;
import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ListOrderRepository extends JpaRepository<ListOrderEntity, Integer> {

    List<ListOrderEntity> findByOrderStatus(OrderStatus status);

    List<ListOrderEntity> findByOrderDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate);
}
