package com.zerobase.order_drinks.service;


import com.zerobase.order_drinks.exception.CustomException;
import com.zerobase.order_drinks.model.dto.ListOrderDto;
import com.zerobase.order_drinks.model.dto.StoreGroup;
import com.zerobase.order_drinks.model.dto.StoreOrderBillDto;
import com.zerobase.order_drinks.model.entity.ListOrderEntity;
import com.zerobase.order_drinks.model.entity.StoreEntity;
import com.zerobase.order_drinks.repository.ListOrderRepository;
import com.zerobase.order_drinks.repository.StoreRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static com.zerobase.order_drinks.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {
    private final ListOrderRepository listOrderRepository;
    private final StoreRepository storeRepository;

    private StoreEntity getStore(String storeName) {
        return storeRepository.findByStoreName(storeName)
                .orElseThrow(() -> new CustomException(NOT_FOUND_STORE_DATA));
    }

    public Page<ListOrderDto> getOrderList(LocalDate start, LocalDate end, Pageable pageable) {
        Page<ListOrderEntity> result = listOrderRepository.findByOrderDateTimeBetween(
                start.atTime(0, 0,0), end.atTime(23, 59,59),
                pageable);

        if(result.isEmpty()){
            throw new CustomException(NOT_EXIST_ORDER_LIST);
        }
        return result.map(m -> new ListOrderDto().toDto(m));
    }

    public StoreOrderBillDto getOrderListByStoreName(
            String storeName, LocalDate startDate, LocalDate endDate, Pageable pageable) {

        StoreEntity store = getStore(storeName);
        List<ListOrderDto> list = getFilterList(startDate, endDate, store);

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), list.size());

        return new StoreOrderBillDto().builder()
                .sum(getTotalPrice(list))
                .orderList(new PageImpl<>(list.subList(start, end), pageable, list.size()))
                .build();
    }

    public Page<StoreGroup> getEachStoreSalesPrice(LocalDate startDate, LocalDate endDate, Pageable pageable){
        Page<StoreEntity> stores = storeRepository.findAll(pageable);
        if(stores.isEmpty()){
            throw new CustomException(NOT_EXIST_STORE_SALES_DATA);
        }
        List<StoreGroup> storeGroupList = new ArrayList<>();

        for(StoreEntity store : stores){
            storeGroupList.add(StoreGroup.builder()
                            .storeName(store.getStoreName())
                            .totalPrice(getTotalPrice(getFilterList(startDate, endDate, store)))
                    .build());
        }

        return new PageImpl<>(storeGroupList);
    }

    private static long getTotalPrice(List<ListOrderDto> page) {
        return page.stream().mapToLong(ListOrderDto::getPrice).sum();
    }

    private static List<ListOrderDto> getFilterList(LocalDate startDate, LocalDate endDate, StoreEntity store) {
        List<ListOrderEntity> filterData =  store.getList().stream()
                .filter(m -> m.getOrderDateTime().isAfter(startDate.atTime(0, 0, 0)) &&
                        m.getOrderDateTime().isBefore(endDate.atTime(23, 59, 59))).toList();

        if(filterData.isEmpty()){
            throw new CustomException(NOT_EXIST_STORE_SALES_DATA);
        }
        return filterData.stream().map(m -> new ListOrderDto().toDto(m)).toList();
    }
}
