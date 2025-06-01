package com.sky.controller.user;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import static com.sky.constant.CacheConstant.SHOP_STATUS_KEY;
import static com.sky.constant.StatusConstant.DISABLE;

// 注意需要设置别名，否则admin和user下的ShopController会产生冲突
@RestController("userShopController")
@RequestMapping("user/shop")
@Api(tags = "C端-商店相关接口")
@Slf4j
public class ShopController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("status")
    @ApiOperation("查询商店营业状态")
    public Result<Integer> getStatus() {
        log.info("设置店铺状态功能");
        String s = stringRedisTemplate.opsForValue().get(SHOP_STATUS_KEY);
        if (s == null || s.isEmpty()) {
            return Result.success(DISABLE);
        }
        return Result.success(Integer.parseInt(s));
    }
}
