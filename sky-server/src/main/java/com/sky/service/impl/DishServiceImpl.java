package com.sky.service.impl;

import cn.hutool.json.JSONUtil;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishMapper;
import com.sky.mapper.FlavorMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.sky.constant.MessageConstant.DISH_BE_RELATED_BY_SETMEAL;
import static com.sky.constant.MessageConstant.DISH_ON_SALE;
import static com.sky.constant.RedisConstant.DISH_CACHE_KEY;
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

    @Resource
    private StringRedisTemplate stringRedisTemplate;


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
        // 删除缓存数据
        String key = DISH_CACHE_KEY + dish.getCategoryId();
        cleanCache(key);
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

        // 4. 删除所有菜品缓存
        cleanCache(DISH_CACHE_KEY + "*");
    }

    /**
     * 根据id查询菜品，同时返回关联的口味数据
     *
     * @param id 查询的id
     * @return 查询到的数据
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
     * @param dishDTO 菜品修改DTO
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

        // 3. 删除所有菜品缓存
        cleanCache(DISH_CACHE_KEY + "*");
    }

    /**
     * 修改菜品状态
     *
     * @param status 将要设置的状态
     * @param id     菜品id
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
        // 删除所有菜品缓存
        cleanCache(DISH_CACHE_KEY + "*");
    }

    @Override
    @Transactional
    public List<DishVO> listWithFlavor(Dish dish) {
        // 1. 先查询 Redis 中是否有对应缓存，根据 categoryId 来存储
        String key = DISH_CACHE_KEY + dish.getCategoryId();
        List<String> strDishVO = stringRedisTemplate.opsForList().range(key, 0, -1);
        List<DishVO> list = new ArrayList<>();
        if (strDishVO != null && !strDishVO.isEmpty()) {
            strDishVO.forEach(each -> list.add(JSONUtil.toBean(each, DishVO.class)));
            return list;
        }

        // 2. 不存在，再查询数据库
        List<Dish> dishes = dishMapper.query(dish);
        for (Dish d : dishes) {
            List<DishFlavor> flavors = flavorMapper.getByDishId(d.getId());
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d, dishVO);
            dishVO.setFlavors(flavors);
            list.add(dishVO);
        }

        // 3. 将数据库查询到的数据放入 Redis
        list.forEach(each -> stringRedisTemplate
                .opsForList()
                .rightPush(key, JSONUtil.toJsonStr(each)));

        return list;
    }

    @Override
    public List<Dish> list(Dish dish) {
        return dishMapper.query(dish);
    }

    /**
     * 清除缓存
     *
     * @param pattern 需要清理缓存的前缀
     */
    private void cleanCache(String pattern) {
        Set<String> keys = stringRedisTemplate.keys(pattern);
        log.info("正在清除Redis缓存: {}", keys);
        stringRedisTemplate.delete(keys);
    }
}
