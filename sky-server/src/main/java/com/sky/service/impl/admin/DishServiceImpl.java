package com.sky.service.impl.admin;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.admin.DishMapper;
import com.sky.mapper.admin.FlavorMapper;
import com.sky.mapper.admin.SetmealDishMapper;
import com.sky.mapper.admin.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.admin.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.sky.constant.MessageConstant.DISH_BE_RELATED_BY_SETMEAL;
import static com.sky.constant.MessageConstant.DISH_ON_SALE;
import static com.sky.constant.StatusConstant.DISABLE;
import static com.sky.constant.StatusConstant.ENABLE;

@Service
@Slf4j
public class DishServiceImpl implements DishService {

    @Resource
    private DishMapper dishMapper;

    @Resource
    private FlavorMapper flavorMapper;

    @Resource
    private SetmealDishMapper setmealDishMapper;

    @Resource
    private SetmealMapper setmealMapper;


    @Override
    // 出现对于多张数据表的操作，要开启事务操作来保证原子性
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        // 0. 将DTO转为实体类
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);

        // 1. 向菜品表中插入数据
        dishMapper.insert(dish);
        Long dishId = dish.getId();

        // 2. 向口味表中插入数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        // 判断口味集合是否存在
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> flavor.setDishId(dishId));
            // 批量插入
            flavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 分页查询
     *
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);

        long total = page.getTotal();
        List<DishVO> records = page.getResult();

        return new PageResult(total, records);
    }

    /**
     * 批量删除
     *
     * @param ids
     */
    @Override
    public void deleteBatch(List<Long> ids) {
        // 1. 判断当前菜品能否删除
        // 1.1 是否在起售中?
        List<Dish> dishes = dishMapper.getBatchByIds(ids);
        for (Dish dish : dishes) {
            if (dish.getStatus().equals(ENABLE)) {
                throw new DeletionNotAllowedException(DISH_ON_SALE);
            }
        }

        // 1.2 是否被套餐关联?
        List<Long> setmealIdByDishIds = setmealDishMapper.getSetmealIdByDishIds(ids);
        if (setmealIdByDishIds != null && !setmealIdByDishIds.isEmpty()) {
            throw new DeletionNotAllowedException(DISH_BE_RELATED_BY_SETMEAL);
        }

        // 2. 可以删除，删除菜品表中的数据
        dishMapper.deleteBatchByIds(ids);

        // 3. 删除口味表中的口味数据
        flavorMapper.deleteBatchByDishIds(ids);
    }

    /**
     * 根据id查询菜品，同时返回关联的口味数据
     *
     * @param id
     * @return
     */
    @Override
    public DishVO queryById(Long id) {
        // 1. 查询菜品数据
        Dish dish = dishMapper.getById(id);

        // 2. 查询口味数据
        List<DishFlavor> flavors = flavorMapper.getByDishId(id);

        // 3. 封装成VO对象返回
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish, dishVO);
        dishVO.setFlavors(flavors);

        return dishVO;
    }

    /**
     * 修改菜品信息和对应的口味数据
     *
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        // 1. 修改菜品信息
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);

        // 2. 修改口味信息：先删除原有口味，才插入新数据
        // 2.1 删除口味表中的数据
        Long dishId = dish.getId();
        ArrayList<Long> ids = new ArrayList<>();
        ids.add(dishId);
        flavorMapper.deleteBatchByDishIds(ids);

        // 2.2 向口味表中插入数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        // 判断口味集合是否存在
        if (flavors != null && !flavors.isEmpty()) {
            flavors.forEach(flavor -> flavor.setDishId(dishId));
            // 批量插入
            flavorMapper.insertBatch(flavors);
        }

    }

    /**
     * 修改菜品状态
     *
     * @param status
     * @param id
     */
    @Override
    public void changeStatus(Integer status, Long id) {
        Dish dish = Dish.builder().id(id).status(status).build();
        dishMapper.update(dish);

        // 如果该菜品停售时，被包含在套餐中，需要将所在的所有套餐也停售
        if (status.equals(DISABLE)) {
            Long dishId = dish.getId();
            List<Long> ids = new ArrayList<>();
            ids.add(dishId);

            List<Long> setmealIds = setmealDishMapper.getSetmealIdByDishIds(ids);
            if (setmealIds != null && !setmealIds.isEmpty()) {
                for (Long setmealId : setmealIds) {
                    Setmeal setmeal = Setmeal.builder().id(setmealId).status(DISABLE).build();
                    setmealMapper.update(setmeal);
                }
            }
        }
    }

    @Override
    public List<Dish> queryByCategoryId(Long categoryId) {
        return dishMapper.queryByCategoryId(categoryId);
    }
}
