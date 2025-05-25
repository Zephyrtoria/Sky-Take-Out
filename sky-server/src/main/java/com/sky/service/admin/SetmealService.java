package com.sky.service.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.vo.SetmealVO;

import java.util.List;

public interface SetmealService {
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    SetmealVO queryById(Long id);

    void insert(SetmealDTO setmealDTO);

    void update(SetmealDTO setmealDTO);

    void startOrStop(Integer status, Long id);

    void deleteBatch(List<Long> ids);
}
