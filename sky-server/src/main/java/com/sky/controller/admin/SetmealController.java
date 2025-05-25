package com.sky.controller.admin;

import com.sky.dto.SetmealDTO;
import com.sky.dto.SetmealPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.admin.SetmealService;
import com.sky.vo.SetmealVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("admin/setmeal")
@Api(tags = "套餐相关接口")
@Slf4j
public class SetmealController {
    @Resource
    private SetmealService setmealService;

    @GetMapping("page")
    @ApiOperation("套餐分页查询功能")
    public Result<PageResult> pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        log.info("套餐分页查询功能: {}", setmealPageQueryDTO);
        PageResult pageResult = setmealService.pageQuery(setmealPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("{id}")
    @ApiOperation("根据id查询套餐功能")
    public Result<SetmealVO> queryById(@PathVariable Long id) {
        log.info("根据id查询套餐功能: {}", id);
        SetmealVO setmealVO = setmealService.queryById(id);
        return Result.success(setmealVO);
    }

    @PostMapping("")
    @ApiOperation("新增套餐功能")
    public Result<String> insert(@RequestBody SetmealDTO setmealDTO) {
        log.info("新增套餐功能: {}", setmealDTO);
        setmealService.insert(setmealDTO);
        return Result.success();
    }

    @PutMapping("")
    @ApiOperation("修改套餐信息功能")
    public Result<String> update(@RequestBody SetmealDTO setmealDTO) {
        log.info("修改套餐信息功能: {}", setmealDTO);
        setmealService.update(setmealDTO);
        return Result.success();
    }

    @PostMapping("status/{status}")
    @ApiOperation("套餐起售、停售功能")
    public Result<String> startOrStop(@PathVariable Integer status, Long id) {
        log.info("套餐起售、停售功能: {}, {}", status, id);
        setmealService.startOrStop(status, id);
        return Result.success();
    }

    @DeleteMapping("")
    @ApiOperation("套餐批量删除功能")
    public Result<String> delete(@RequestParam List<Long> ids) {
        log.info("套餐批量删除功能: {}", ids);
        setmealService.deleteBatch(ids);
        return Result.success();
    }
}
