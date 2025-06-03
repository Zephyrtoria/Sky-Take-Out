package com.sky.task;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.beans.Transient;
import java.time.LocalDateTime;
import java.util.List;

import static com.sky.constant.MessageConstant.ORDER_TIME_OUT;

@Component
@Slf4j
public class OrderTask {

    @Resource
    private OrderMapper orderMapper;

    /**
     * 处理超时订单
     */
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void processTimeOutOrder() {
        LocalDateTime now = LocalDateTime.now();
        log.info("处理超时订单: {}", now);
        // select * from order where status = 1 and order_time < #{orderTime}
        List<Orders> list = orderMapper.getByStatusAndOrderTime(Orders.PENDING_PAYMENT, now.minusMinutes(15L));
        if (list != null && !list.isEmpty()) {
            list.forEach(order -> {
                order.setStatus(Orders.CANCELLED);
                order.setCancelReason(ORDER_TIME_OUT);
                order.setCancelTime(now);
                orderMapper.update(order);
            });
        }
    }

    @Scheduled(cron = "0 0 1 * * ?")
    @Transactional
    public void processDeliveryOrder() {
        LocalDateTime now = LocalDateTime.now();
        log.info("处理派送中的订单: {}", now);

        List<Orders> list = orderMapper.getByStatusAndOrderTime(Orders.DELIVERY_IN_PROGRESS, now.minusHours(1L));
        if (list != null && !list.isEmpty()) {
            list.forEach(order -> {
                order.setStatus(Orders.COMPLETED);
                orderMapper.update(order);
            });
        }
    }
}
