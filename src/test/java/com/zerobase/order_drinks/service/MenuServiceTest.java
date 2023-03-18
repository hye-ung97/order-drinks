package com.zerobase.order_drinks.service;

import com.zerobase.order_drinks.exception.CustomException;
import com.zerobase.order_drinks.exception.ErrorCode;
import com.zerobase.order_drinks.model.dto.Menu;
import com.zerobase.order_drinks.model.entity.MenuEntity;
import com.zerobase.order_drinks.repository.MenuRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class MenuServiceTest {

    @Mock
    private MenuRepository menuRepository;
    @InjectMocks
    private MenuService menuService;

    @Test
    @DisplayName("메뉴 등록  - 실패")
    void menuRegisterFail() {
        //given
        Menu menu = new Menu();
        menu.setMenuName("아메리카노");
        menu.setPrice(4100);

        given(menuRepository.existsByMenuName(anyString())).willReturn(true);

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> menuService.menuRegister(menu));

        //then
        assertEquals(ErrorCode.EXIST_MENU, exception.getErrorCode());
    }

    @Test
    @DisplayName("메뉴 등록  - 성공")
    void menuRegisterSuccess() {
        //given
        Menu menu = new Menu();
        menu.setMenuName("아메리카노");
        menu.setPrice(4100);

        MenuEntity menuEntity = MenuEntity.builder()
                .registerDateTime(LocalDateTime.now())
                .menuName("아메리카노")
                .price(4100)
                .build();
        given(menuRepository.existsByMenuName(anyString())).willReturn(false);
        given(menuRepository.save(any())).willReturn(menuEntity);

        //when
        var result = menuService.menuRegister(menu);

        //then
        assertEquals(menu.getMenuName(), result.getMenuName());
        assertEquals(menu.getPrice(), result.getPrice());
    }

    @Test
    @DisplayName("메뉴리스트 보기 - 없음")
    void menuListFail() {
        //given
        given(menuRepository.findAll(any(Pageable.class))).willReturn(Page.empty());

        //when
        CustomException exception = assertThrows(CustomException.class,
                () -> menuService.menuList(Pageable.ofSize(10)));

        //then
        assertEquals(ErrorCode.NOT_EXIST_MENU_LIST, exception.getErrorCode());

    }

    @Test
    @DisplayName("메뉴리스트 보기 - 성공")
    void menuListSuccess() {
        //given
        MenuEntity menu = MenuEntity.builder()
                .menuName("아메리카노")
                .price(4100)
                .build();
        MenuEntity menu2 = MenuEntity.builder()
                .menuName("라떼")
                .price(4500)
                .build();
        List<MenuEntity> list = new ArrayList<>();
        list.add(menu);
        list.add(menu2);
        Page<MenuEntity> page = new PageImpl<>(list);

        given(menuRepository.findAll(any(Pageable.class))).willReturn(page);

        //when
        var result = menuService.menuList(Pageable.ofSize(10));
        List<Menu> menuList = result.stream().toList();

        //then
        assertEquals(2, result.getSize());
        assertEquals("아메리카노", menuList.get(0).getMenuName());
        assertEquals(4100, menuList.get(0).getPrice());
        assertEquals("라떼", menuList.get(1).getMenuName());
        assertEquals(4500, menuList.get(1).getPrice());

    }
}