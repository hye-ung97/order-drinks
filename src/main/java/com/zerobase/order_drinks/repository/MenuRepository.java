package com.zerobase.order_drinks.repository;

import com.zerobase.order_drinks.model.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MenuRepository extends JpaRepository<MenuEntity, Integer> {

    boolean existsByMenuName(String menuName);
    Optional<MenuEntity> findByMenuName(String menuName);
}
