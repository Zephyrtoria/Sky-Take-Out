package com.sky.service.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.vo.DishVO;

import java.util.List;

public interface DishService {

    void saveWithFlavor(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    void deleteBatch(List<Long> ids);

    DishVO queryById(Long id);

    void updateWithFlavor(DishDTO dishDTO);

    void changeStatus(Integer status, Long id);

    List<Dish> queryByCategoryId(Long categoryId);
}
