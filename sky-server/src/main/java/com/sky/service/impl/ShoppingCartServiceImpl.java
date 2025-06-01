package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ShoppingCartService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShoppingCartServiceImpl implements ShoppingCartService {

    @Resource
    private ShoppingCartMapper shoppingCartMapper;
    @Resource
    private DishMapper dishMapper;
    @Resource
    private SetmealMapper setmealMapper;

    @Override
    @Transactional
    public void insert(ShoppingCartDTO shoppingCartDTO) {
        // 0. 获取用户
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = new ShoppingCart();
        BeanUtil.copyProperties(shoppingCartDTO, shoppingCart);
        shoppingCart.setUserId(userId);

        // 1. 判断购物车中是否已经存在该商品
        List<ShoppingCart> list = shoppingCartMapper.query(shoppingCart);

        // 2. 存在，number++ -> update
        if (list != null && !list.isEmpty()) {
            ShoppingCart cart = list.get(0);
            cart.setNumber(cart.getNumber() + 1);
            shoppingCartMapper.update(cart);
        } else {
            // 3. 不存在，insert
            // 4. 判断本次添加的是菜品还是套餐
            Long dishId = shoppingCart.getDishId();
            if (dishId != null) {
                // 添加菜品
                Dish dish = dishMapper.getById(dishId);
                shoppingCart.setName(dish.getName());
                shoppingCart.setImage(dish.getImage());
                shoppingCart.setAmount(dish.getPrice());
            } else {
                // 添加套餐
                Long setmealId = shoppingCart.getSetmealId();
                Setmeal setmeal = setmealMapper.queryById(setmealId);
                shoppingCart.setName(setmeal.getName());
                shoppingCart.setImage(setmeal.getImage());
                shoppingCart.setAmount(setmeal.getPrice());
            }
            shoppingCart.setCreateTime(LocalDateTime.now());
            shoppingCart.setNumber(1);
            shoppingCartMapper.insert(shoppingCart);
        }
    }

    @Override
    public List<ShoppingCart> list(Long userId) {
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        return shoppingCartMapper.query(shoppingCart);
    }

    @Override
    @Transactional
    public void sub(ShoppingCartDTO shoppingCartDTO) {
        // 1. 查找
        Long userId = BaseContext.getCurrentId();
        ShoppingCart cart = new ShoppingCart();
        BeanUtil.copyProperties(shoppingCartDTO, cart);
        cart.setUserId(userId);

        List<ShoppingCart> list = shoppingCartMapper.query(cart);

        // 2. 修改number
        if (list != null && !list.isEmpty()) {
            cart = list.get(0);
            cart.setNumber(cart.getNumber() - 1);
            if (cart.getNumber() <= 0) {
                // 3. 如果归零则删除
                shoppingCartMapper.deleteByBatch(cart);
            } else {
                shoppingCartMapper.update(cart);
            }
        }
    }

    @Override
    public void clean(Long userId) {
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        shoppingCartMapper.deleteByBatch(shoppingCart);
    }

}
