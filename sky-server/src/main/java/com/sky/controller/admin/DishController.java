package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.admin.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {

    @Resource
    private DishService dishService;

    @PostMapping("")
    @ApiOperation("新增菜品功能")
    public Result<String> insertDish(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品: {}", dishDTO);
        dishService.saveWithFlavor(dishDTO);
        return Result.success();
    }

    @GetMapping("page")
    @ApiOperation("菜品分页查询功能")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        log.info("菜品分页查询: {}", dishPageQueryDTO);
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @DeleteMapping("")
    @ApiOperation("批量删除菜品功能")
    public Result<String> delete(@RequestParam List<Long> ids) {
        log.info("删除菜品: {}", ids);
        dishService.deleteBatch(ids);
        return Result.success();
    }

    @GetMapping("{id}")
    @ApiOperation("根据id查询菜品功能")
    public Result<DishVO> queryById(@PathVariable Long id) {
        log.info("根据id查询菜品: {}", id);
        DishVO dishVo = dishService.queryById(id);
        return Result.success(dishVo);
    }

    @PutMapping("")
    @ApiOperation("修改菜品信息功能")
    public Result<String> update(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品信息: {}", dishDTO);
        dishService.updateWithFlavor(dishDTO);
        return Result.success();
    }

    @PostMapping("status/{status}")
    @ApiOperation("菜品起售、停售功能")
    public Result<String> changeStatus(@PathVariable Integer status, Long id) {
        log.info("菜品起售、停售: {} {}", status, id);
        dishService.changeStatus(status, id);
        return Result.success();
    }

    @GetMapping("list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> queryByCategoryId(Long categoryId) {
        log.info("根据分类id查询菜品");
        List<Dish> dishes = dishService.queryByCategoryId(categoryId);
        return Result.success(dishes);
    }
}
