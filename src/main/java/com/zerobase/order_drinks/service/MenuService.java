package com.zerobase.order_drinks.service;


import com.zerobase.order_drinks.exception.CustomException;
import com.zerobase.order_drinks.model.dto.Menu;
import com.zerobase.order_drinks.model.dto.MenuInventory;
import com.zerobase.order_drinks.model.entity.MenuEntity;
import com.zerobase.order_drinks.repository.MenuRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.zerobase.order_drinks.exception.ErrorCode.*;

@Slf4j
@Service
@AllArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;

    @Transactional
    public MenuEntity menuRegister(Menu menu){
        boolean exists = menuRepository.existsByMenuName(menu.getMenuName());
        if(exists){
            throw new CustomException(EXIST_MENU);
        }

        return menuRepository.save(menu.toEntity());
    }

    public Page<Menu> menuList(Pageable pageable) {
        Page<MenuEntity> menuEntityPage = menuRepository.findAll(pageable);
        if(menuEntityPage.isEmpty()){
            throw new CustomException(NOT_EXIST_MENU_LIST);
        }

        return menuEntityPage.map(m -> new Menu().menuDto(m.getMenuName(), m.getPrice()));
    }

    public MenuInventory setInventory(MenuInventory menuInventory) {
        MenuEntity result = menuRepository.findByMenuName(menuInventory.getMenuName())
                .orElseThrow(() -> new CustomException(NOT_EXIST_MENU));

        int quantity = result.getQuantity() + menuInventory.getQuantity();
        result.setQuantity(quantity);
        result.setUpdateDateTime(LocalDateTime.now());
        menuRepository.save(result);

        menuInventory.setQuantity(quantity);
        return menuInventory;
    }

    public Page<MenuInventory> getInventory(Pageable pageable) {
        Page<MenuEntity> result = menuRepository.findAll(pageable);
        if(result.isEmpty()){
            throw new CustomException(NOT_EXIST_MENU_LIST);
        }

        return result.map(m -> MenuInventory.builder()
                .menuName(m.getMenuName())
                .quantity(m.getQuantity())
                .build());
    }
}
