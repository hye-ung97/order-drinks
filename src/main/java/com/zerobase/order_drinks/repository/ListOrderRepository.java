package com.zerobase.order_drinks.repository;

import com.zerobase.order_drinks.model.constants.OrderStatus;
import com.zerobase.order_drinks.model.dto.StoreGroupDtoImp;
import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ListOrderRepository extends JpaRepository<ListOrderEntity, Integer> {

    List<ListOrderEntity> findByOrderStatus(OrderStatus status);

    List<ListOrderEntity> findByOrderDateTimeBetween(LocalDateTime startDate, LocalDateTime endDate);

    Page<ListOrderEntity> findByUserName(String userName, Pageable pageable);

    List<ListOrderEntity> findByStoreAndOrderDateTimeBetween (String storeName, LocalDateTime start, LocalDateTime end);

    @Query(value = "select lo.store as storeName, SUM(lo.price) as totalPrice" +
            " from list_order lo" +
            " where DATE(order_date_time) between :startDate and :endDate" +
            " group by lo.store order by totalPrice DESC", nativeQuery = true)
    List<StoreGroupDtoImp> findByStoreGroupSalesPrice(@Param("startDate") LocalDate start, @Param("endDate") LocalDate end);
}
