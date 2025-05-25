package com.sky.service.impl.admin;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Setmeal;
import com.sky.entity.SetmealDish;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.exception.SetmealEnableFailedException;
import com.sky.mapper.admin.DishMapper;
import com.sky.mapper.admin.SetmealDishMapper;
import com.sky.mapper.admin.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.admin.SetmealService;
import com.sky.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

import static com.sky.constant.MessageConstant.SETMEAL_ENABLE_FAILED;
import static com.sky.constant.MessageConstant.SETMEAL_ON_SALE;
import static com.sky.constant.StatusConstant.DISABLE;
import static com.sky.constant.StatusConstant.ENABLE;

@Service
public class SetMealServiceImpl implements SetmealService {
    @Resource
    private DishMapper dishMapper;

    @Resource
    private SetmealMapper setmealMapper;

    @Resource
    private SetmealDishMapper setmealDishMapper;

    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {

        PageHelper.startPage(setmealPageQueryDTO.getPage(), setmealPageQueryDTO.getPageSize());
        Page<Setmeal> page = setmealMapper.pageQuery(setmealPageQueryDTO);

        long total = page.getTotal();
        List<Setmeal> result = page.getResult();

        return new PageResult(total, result);
    }

    @Override
    public SetmealVO queryById(Long id) {
        // 1. 查询setmeal
        Setmeal setmeal = setmealMapper.queryById(id);

        // 2. 查询setmeal-dish
        List<SetmealDish> setmealDishes = setmealDishMapper.getSetmealDishBySetmealId(id);

        // 3. 封装VO类
        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal, setmealVO);
        setmealVO.setSetmealDishes(setmealDishes);
        return setmealVO;
    }

    @Override
    @Transactional
    public void insert(SetmealDTO setmealDTO) {
        // 1. 将DTO拆分为setmeal表和setmeal_dish表
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        // 2. 存入 setmeal 表，并获取id回显
        setmealMapper.insert(setmeal);

        // 3. 存入setmeal_dish表
        setmealDishMapper.insertBatch(setmealDishes, setmeal.getId());
    }

    @Override
    @Transactional
    public void update(SetmealDTO setmealDTO) {
        // 1. 分离setmeal和setmeal_dish
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO, setmeal);

        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();

        // 2. update setmeal
        setmealMapper.update(setmeal);

        // 3. 删除setmeal_dish中旧数据，插入新数据
        List<Long> ids = new ArrayList<>();
        Long setmealId = setmeal.getId();
        ids.add(setmealId);
        setmealDishMapper.deleteBatchBySetmealIds(ids);
        setmealDishMapper.insertBatch(setmealDishes, setmealId);
    }

    @Override
    @Transactional
    public void startOrStop(Integer status, Long id) {
        // 1. 判断套餐内的所有菜品是否都处于起售状态
        List<SetmealDish> setmealDishes = setmealDishMapper.getSetmealDishBySetmealId(id);

        List<Long> dishIds = new ArrayList<>();
        for (SetmealDish setmealDish : setmealDishes) {
            dishIds.add(setmealDish.getDishId());
        }
        List<Dish> dishes = dishMapper.getBatchByIds(dishIds);
        for (Dish dish : dishes) {
            if (dish.getStatus().equals(DISABLE)) {
                throw new SetmealEnableFailedException(SETMEAL_ENABLE_FAILED);
            }
        }

        // 2. 修改状态
        Setmeal setmeal = Setmeal.builder().status(status).id(id).build();
        setmealMapper.update(setmeal);
    }

    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 1. 判断是否在起售状态
        List<Setmeal> setmeals = setmealMapper.getBatchByIds(ids);
        for (Setmeal setmeal : setmeals) {
            if (setmeal.getStatus().equals(ENABLE)) {
                throw new DeletionNotAllowedException(SETMEAL_ON_SALE);
            }
        }

        // 2. 删除setmeal
        setmealMapper.deleteBatchByIds(ids);

        // 3. 删除setmeal_dish
        setmealDishMapper.deleteBatchBySetmealIds(ids);
    }
}
