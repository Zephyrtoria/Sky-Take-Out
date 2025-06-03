package com.sky.controller.user;

import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.OrderService;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController("userOrderController")
@RequestMapping("user/order")
@Slf4j
@Api(tags = "C端-用户订单相关接口")
public class OrderController {

    @Resource
    private OrderService orderService;

    @PostMapping("submit")
    @ApiOperation("用户下单功能")
    public Result<OrderSubmitVO> submit(@RequestBody OrdersSubmitDTO ordersSubmitDTO) {
        log.info("用户下单功能: {}", ordersSubmitDTO);
        OrderSubmitVO orderSubmitVO = orderService.submitOrder(ordersSubmitDTO);
        return Result.success(orderSubmitVO);
    }

    @PutMapping("payment")
    @ApiOperation("订单支付")
    public Result<OrderPaymentVO> payment(@RequestBody OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        log.info("订单支付：{}", ordersPaymentDTO);
        OrderPaymentVO orderPaymentVO = orderService.payment(ordersPaymentDTO);
        log.info("生成预支付交易单：{}", orderPaymentVO);
        return Result.success(orderPaymentVO);
    }

    @GetMapping("orderDetail/{id}")
    @ApiOperation("获取订单详情")
    public Result<OrderVO> getDetail(@PathVariable Long id) {
        log.info("获取订单详情: {}", id);
        OrderVO orderVO = orderService.getDetail(id);
        return Result.success(orderVO);
    }

    @GetMapping("historyOrders")
    @ApiOperation("查询历史订单")
    public Result<PageResult> historyOrders(OrdersPageQueryDTO ordersPageQueryDTO) {
        log.info("查询历史订单: {}", ordersPageQueryDTO);
        PageResult pageResult = orderService.history(ordersPageQueryDTO);
        return Result.success(pageResult);
    }

    @PutMapping("cancel/{id}")
    @ApiOperation("取消订单")
    public Result<String> cancel(@PathVariable Long id) {
        log.info("取消订单: {}", id);
        String cancelReason = "取消订单没有理由！";
        orderService.cancel(id, cancelReason);
        return Result.success();
    }

    @PostMapping("repetition/{id}")
    @ApiOperation("再来一单")
    public Result<String> repetition(@PathVariable Long id) {
        log.info("再来一单: {}", id);
        orderService.repetition(id);
        return Result.success();
    }
}
