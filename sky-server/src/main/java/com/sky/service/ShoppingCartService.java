package com.sky.service;

import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    void insert(ShoppingCartDTO shoppingCartDTO);

    List<ShoppingCart> list(Long userId);

    void sub(ShoppingCartDTO shoppingCartDTO);

    void clean(Long userId);
}
