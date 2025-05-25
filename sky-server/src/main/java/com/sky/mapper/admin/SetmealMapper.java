package com.sky.mapper.admin;

import com.github.pagehelper.Page;
import com.sky.annotation.AutoFill;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Setmeal;
import com.sky.enumeration.OperationType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface SetmealMapper {

    /**
     * 根据分类id查询套餐的数量
     *
     * @param id
     * @return
     */
    @Select("select count(id) from setmeal where category_id = #{categoryId}")
    Integer countByCategoryId(Long id);


    /**
     * 修改套餐信息
     *
     * @param setmeal
     */
    @AutoFill(OperationType.UPDATE)
    void update(Setmeal setmeal);

    /**
     * 分页查询
     *
     * @param setmealPageQueryDTO
     * @return
     */
    Page<Setmeal> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 根据id查询
     *
     * @param id
     * @return
     */
    @Select("select * from setmeal where id = #{id}")
    Setmeal queryById(Long id);

    /**
     * 新增数据
     *
     * @param setmeal
     */
    @AutoFill(OperationType.INSERT)
    void insert(Setmeal setmeal);

    /**
     * 根据id批量查询
     * @param ids
     * @return
     */
    List<Setmeal> getBatchByIds(List<Long> ids);

    /**
     * 根据id批量删除
     * @param ids
     */
    void deleteBatchByIds(List<Long> ids);
}
