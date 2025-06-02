package com.sky.controller.admin;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Slf4j
@RequestMapping("admin/order")
@Api(tags = "商户端订单管理相关接口")
public class OrderController {
    @Resource
    private OrderService orderService;

    @ApiOperation("查询订单详情")
    @GetMapping("details/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        log.info("查询订单详情: {}", id);
        OrderVO orderVO = orderService.getDetail(id);
        return Result.success(orderVO);
    }

    @GetMapping("conditionSearch")
    @ApiOperation("订单搜索")
    public Result<PageResult> search(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("订单搜索: {}", ordersPageQueryDTO);
        PageResult pageResult = orderService.search(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @GetMapping("statistics")
    @ApiOperation("各个状态的订单数量统计")
    public Result<OrderStatisticsVO> statistics() {
        log.info("各个状态的订单数量统计");
        OrderStatisticsVO orderStatisticsVO = orderService.statistics();
        return Result.success(orderStatisticsVO);
    }
}
