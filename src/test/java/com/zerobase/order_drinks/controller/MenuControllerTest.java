package com.zerobase.order_drinks.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.order_drinks.exception.CustomException;
import com.zerobase.order_drinks.exception.ErrorCode;
import com.zerobase.order_drinks.model.dto.Menu;
import com.zerobase.order_drinks.model.dto.MenuInventory;
import com.zerobase.order_drinks.model.entity.MenuEntity;
import com.zerobase.order_drinks.security.SecurityConfiguration;
import com.zerobase.order_drinks.security.TokenProvider;
import com.zerobase.order_drinks.service.MenuService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = MenuController.class,
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfiguration.class)}
)
class MenuControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TokenProvider tokenProvider;
    @MockBean
    private MenuService menuService;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username="admin",roles={"USER","ADMIN"})
    @DisplayName("메뉴 등록 실패")
    void menuRegisterFail() throws Exception {
        //given
        Menu menu = new Menu();
        menu.setMenuName("아메리카노");
        menu.setPrice(4100);
        given(menuService.menuRegister(any()))
                .willThrow(new CustomException(ErrorCode.EXIST_MENU));

        //when
        //then
        mockMvc.perform(post("/menu/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(objectMapper.writeValueAsString(menu)))
                .andExpect(jsonPath("$.code")
                        .value("EXIST_MENU"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="admin",roles={"USER","ADMIN"})
    @DisplayName("메뉴 등록 성공")
    void menuRegisterSuccess() throws Exception {

        Menu menu = new Menu();
        menu.setMenuName("아메리카노");
        menu.setPrice(4100);
        given(menuService.menuRegister(any())).willReturn(menu.toEntity());

        //when
        //then
        mockMvc.perform(post("/menu/register").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_UTF8)
                        .content(objectMapper.writeValueAsString(menu)))
                .andExpect(jsonPath("$.price")
                        .value(4100))
                .andExpect(jsonPath("$.menuName")
                        .value("아메리카노"))
                .andExpect(status().isOk());

    }

    @Test
    @WithMockUser
    @DisplayName("메뉴리스트 가져오기 - 실패")
    void menuListFail() throws Exception {
        //given
        given(menuService.menuList(any()))
                .willThrow(new CustomException(ErrorCode.NOT_EXIST_MENU_LIST));

        //when

        //then
        mockMvc.perform(get("/menu/list").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON_UTF8))
                .andExpect(jsonPath("$.code")
                        .value("NOT_EXIST_MENU_LIST"))
                .andExpect(status().isOk());

    }

    @Test
    @WithMockUser
    @DisplayName("메뉴리스트 가져오기 - 성공")
    void menuListSuccess() throws Exception {
        //given
        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "name";
        Sort sort = Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(pageNumber, pageSize, sort);

        Menu menu1 = new Menu();
        menu1.setMenuName("아메리카노");
        menu1.setPrice(4100);
        Menu menu2 = new Menu();
        menu2.setMenuName("라떼");
        menu2.setPrice(4500);

        List<Menu> menuList = List.of(menu1, menu2);

        Page<Menu> page = new PageImpl<Menu>(menuList, pageable, 10);

        given(menuService.menuList(any())).willReturn(page);

        //when

        //then
        mockMvc.perform(get("/menu/list").with(csrf()))
                .andExpect(jsonPath("$.content[0].menuName")
                        .value("아메리카노"))
                .andExpect(jsonPath("$.content[0].price")
                        .value(4100))
                .andExpect(jsonPath("$.content[1].menuName")
                        .value("라떼"))
                .andExpect(jsonPath("$.content[1].price")
                        .value(4500))
                .andExpect(status().isOk());

    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    @DisplayName("재고 관리 - 실패")
    void inventoryManagementFail() throws Exception {
        //given
        MenuInventory inventory = MenuInventory.builder()
                .menuName("아메리카노")
                .quantity(5)
                .build();

        MenuEntity menu = MenuEntity.builder()
                .menuName("아메리카노")
                .quantity(1)
                .build();

        given(menuService.setInventory(any()))
                .willThrow(new CustomException(ErrorCode.NOT_EXIST_MENU));

        //when
        //then
        mockMvc.perform(post("/menu/inventory").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inventory)))
                .andExpect(jsonPath("$.code").value("NOT_EXIST_MENU"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    @DisplayName("재고 관리 - 성공")
    void inventoryManagementSuccess() throws Exception {
        //given
        MenuInventory inventory = MenuInventory.builder()
                .menuName("아메리카노")
                .quantity(5)
                .build();

        MenuEntity menu = MenuEntity.builder()
                .menuName("아메리카노")
                .quantity(1)
                .build();

        given(menuService.setInventory(any())).willReturn(MenuInventory.builder()
                        .quantity(6)
                        .menuName("아메리카노")
                .build());
        //when
        //then
        mockMvc.perform(post("/menu/inventory").with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inventory)))
                .andExpect(jsonPath("$.menuName").value("아메리카노"))
                .andExpect(jsonPath("$.quantity").value(6))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    @DisplayName("재고리스트 가져오기 - 실패")
    void inventoryListFail() throws Exception {
        //given
        given(menuService.getInventory(any(Pageable.class)))
                .willThrow(new CustomException(ErrorCode.NOT_EXIST_MENU_LIST));
        //when
        //then
        mockMvc.perform(get("/menu/inventory").with(csrf()))
                .andExpect(jsonPath("$.code").value("NOT_EXIST_MENU_LIST"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username="admin",roles={"ADMIN"})
    @DisplayName("재고리스트 가져오기 - 성공")
    void inventoryListSuccess() throws Exception {
        //given
        int pageNumber = 0;
        int pageSize = 10;
        String sortBy = "name";
        Sort sort = Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(pageNumber, pageSize, sort);

        List<MenuInventory> menuEntityList = List.of(
                MenuInventory.builder()
                        .menuName("아메리카노")
                        .quantity(1)
                        .build(),
                MenuInventory.builder()
                        .menuName("라떼")
                        .quantity(2)
                        .build()
                );
        Page<MenuInventory> page = new PageImpl<>(menuEntityList);

        given(menuService.getInventory(any(Pageable.class))).willReturn(page);

        //when
        //then
        mockMvc.perform(get("/menu/inventory").with(csrf()))
                .andExpect(jsonPath("$.content[0].menuName")
                        .value("아메리카노"))
                .andExpect(jsonPath("$.content[0].quantity").value(1))
                .andExpect(jsonPath("$.content[1].menuName")
                        .value("라떼"))
                .andExpect(jsonPath("$.content[1].quantity").value(2))
                .andExpect(jsonPath("$.numberOfElements").value(2))
                .andExpect(status().isOk());
    }
}