package com.zerobase.order_drinks.service;


import com.zerobase.order_drinks.exception.CustomException;
import com.zerobase.order_drinks.model.dto.Menu;
import com.zerobase.order_drinks.model.entity.MenuEntity;
import com.zerobase.order_drinks.repository.MenuRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import static com.zerobase.order_drinks.exception.ErrorCode.EXIST_MENU;
import static com.zerobase.order_drinks.exception.ErrorCode.NOT_EXIST_MENU_LIST;

@Slf4j
@Service
@AllArgsConstructor
public class MenuService {
    private final MenuRepository menuRepository;

    @Transactional
    public MenuEntity menuRegister(Menu menu){
        boolean exists = this.menuRepository.existsByMenuName(menu.getMenuName());
        if(exists){
            throw new CustomException(EXIST_MENU);
        }

        var result = this.menuRepository.save(menu.toEntity());

        return result;
    }

    public Page<Menu> menuList(Pageable pageable) {
        Page<MenuEntity> menuEntityPage = this.menuRepository.findAll(pageable);
        if(menuEntityPage.isEmpty()){
            throw new CustomException(NOT_EXIST_MENU_LIST);
        }

        Page<Menu> menuPage = menuEntityPage.map(m -> new Menu().menuDto(m.getMenuName(), m.getPrice()));

        return menuPage;
    }

}
