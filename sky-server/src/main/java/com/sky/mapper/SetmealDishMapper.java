package com.sky.mapper;

import com.sky.entity.SetmealDish;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据id批量查询
     *
     * @param ids
     * @return
     */
    List<Long> getSetmealIdByDishIds(List<Long> ids);

    @Select("select * from setmeal_dish where setmeal_id = #{setmealId}")
    List<SetmealDish> getSetmealDishBySetmealId(Long setmealId);


    /**
     * 批量插入
     *
     * @param setmealDishes
     */
    void insertBatch(List<SetmealDish> setmealDishes, Long setmealId);

    /**
     * 根据套餐id批量删除
     *
     * @param setmealIds
     */
    void deleteBatchBySetmealIds(List<Long> setmealIds);
}
