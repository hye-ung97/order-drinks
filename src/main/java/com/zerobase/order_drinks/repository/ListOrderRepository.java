package com.zerobase.order_drinks.repository;

import com.zerobase.order_drinks.model.constants.OrderStatus;
import com.zerobase.order_drinks.model.dto.StoreGroupDto;
import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Repository
public interface ListOrderRepository extends JpaRepository<ListOrderEntity, Integer> {

    Page<ListOrderEntity> findByOrderStatus(OrderStatus status, Pageable pageable);

    Page<ListOrderEntity> findByOrderDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Page<ListOrderEntity> findByUserName(String userName, Pageable pageable);

    Page<ListOrderEntity> findByStoreAndOrderDateTimeBetween (String storeName, LocalDateTime start, LocalDateTime end, Pageable pageable);

    @Query(value = "select lo.store as storeName, SUM(lo.price) as totalPrice" +
            " from list_order lo" +
            " where DATE(order_date_time) between :startDate and :endDate" +
            " group by lo.store order by totalPrice DESC", nativeQuery = true)
    Page<StoreGroupDto> findByStoreGroupSalesPrice(@Param("startDate") LocalDate start, @Param("endDate") LocalDate end, Pageable pageable);
}
