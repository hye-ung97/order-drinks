package com.zerobase.order_drinks.repository;

import com.zerobase.order_drinks.model.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuRepository extends JpaRepository<MenuEntity, Integer> {

    boolean existsByMenuName(String menuName);
}
