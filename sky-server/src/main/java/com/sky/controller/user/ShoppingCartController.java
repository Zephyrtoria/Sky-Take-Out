package com.sky.controller.user;

import com.sky.context.BaseContext;
import com.sky.dto.ShoppingCartDTO;
import com.sky.entity.ShoppingCart;
import com.sky.result.Result;
import com.sky.service.ShoppingCartService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Slf4j
@RequestMapping("user/shoppingCart")
@Api(tags = "C端-购物车相关接口")
public class ShoppingCartController {

    @Resource
    private ShoppingCartService shoppingCartService;

    @PostMapping("add")
    @ApiOperation("购物车添加菜品功能")
    public Result<String> insert(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("添加购物车功能: {}", shoppingCartDTO);
        shoppingCartService.insert(shoppingCartDTO);
        return Result.success();
    }

    @GetMapping("list")
    @ApiOperation("查看购物车功能")
    // @Cacheable(cacheNames = SHOPPINGCART_CACHE_NAME, key = )
    public Result<List<ShoppingCart>> list() {
        Long userId = BaseContext.getCurrentId();
        log.info("查看购物车功能: {}", userId);
        List<ShoppingCart> list = shoppingCartService.list(userId);
        return Result.success(list);
    }

    @PostMapping("sub")
    @ApiOperation("购物车减少菜品功能")
    public Result<String> sub(@RequestBody ShoppingCartDTO shoppingCartDTO) {
        log.info("购物车减少菜品功能: {}", shoppingCartDTO);
        shoppingCartService.sub(shoppingCartDTO);
        return Result.success();
    }

    @DeleteMapping("clean")
    @ApiOperation("清空购物车内容功能")
    public Result<String> clean() {
        Long userId = BaseContext.getCurrentId();
        log.info("清空购物车内容功能: {}", userId);
        shoppingCartService.clean(userId);
        return Result.success();
    }
}
