package com.sky.controller.admin;

import com.sky.result.Result;
import com.sky.service.admin.ShopService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("admin/shop")
@Api(tags = "管理端店铺相关接口")
@Slf4j
public class ShopController {

    @Resource
    private ShopService shopService;

    @GetMapping("status")
    @ApiOperation("获取店铺状态功能")
    public Result<Integer> getStatus() {
        log.info("获取店铺状态功能");
        Integer status = shopService.getStatus();
        return Result.success(status);
    }

    @PutMapping("{status}")
    @ApiOperation("设置店铺状态功能")
    public Result<String> setStatus(@PathVariable Integer status) {
        log.info("设置店铺状态功能: {}", status);
        shopService.setStatus(status);
        return Result.success();
    }
}
