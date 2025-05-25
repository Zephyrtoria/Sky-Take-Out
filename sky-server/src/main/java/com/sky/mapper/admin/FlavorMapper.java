package com.sky.mapper.admin;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface FlavorMapper {
    /**
     * 批量插入口味数据
     *
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据dishId批量删除关联口味
     *
     * @param ids
     */
    void deleteBatchByDishIds(List<Long> ids);

    /**
     *
     * @param dishId
     * @return
     */
    List<DishFlavor> getByDishId(Long dishId);
}
