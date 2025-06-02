package com.sky.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.context.BaseContext;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.dto.OrdersPaymentDTO;
import com.sky.dto.OrdersSubmitDTO;
import com.sky.entity.*;
import com.sky.exception.AddressBookBusinessException;
import com.sky.exception.ShoppingCartBusinessException;
import com.sky.mapper.*;
import com.sky.result.PageResult;
import com.sky.service.OrderService;
import com.sky.utils.WeChatPayUtil;
import com.sky.vo.OrderPaymentVO;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderSubmitVO;
import com.sky.vo.OrderVO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.sky.constant.MessageConstant.ADDRESS_BOOK_IS_NULL;
import static com.sky.constant.MessageConstant.SHOPPING_CART_IS_NULL;

@Service
public class OrderServiceImpl implements OrderService {
    @Resource
    private OrderMapper orderMapper;
    @Resource
    private OrderDetailMapper orderDetailMapper;
    @Resource
    private AddressBookMapper addressBookMapper;
    @Resource
    private ShoppingCartMapper shoppingCartMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private WeChatPayUtil weChatPayUtil;

    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        // 0. 校验数据，处理业务异常
        // 0.1 收货地址为空
        Long addressBookId = ordersSubmitDTO.getAddressBookId();
        AddressBook addressBook = addressBookMapper.getById(addressBookId);
        if (addressBook == null) {
            throw new AddressBookBusinessException(ADDRESS_BOOK_IS_NULL);
        }

        // 0.2 购物车地址为空
        Long userId = BaseContext.getCurrentId();
        ShoppingCart shoppingCart = ShoppingCart.builder().userId(userId).build();
        List<ShoppingCart> list = shoppingCartMapper.query(shoppingCart);
        if (list == null || list.isEmpty()) {
            throw new ShoppingCartBusinessException(SHOPPING_CART_IS_NULL);
        }

        // 1. 向order表插入一条数据
        Orders orders = new Orders();
        BeanUtil.copyProperties(ordersSubmitDTO, orders);
        orders.setOrderTime(LocalDateTime.now());
        orders.setPayStatus(Orders.UN_PAID);
        orders.setStatus(Orders.PENDING_PAYMENT);
        orders.setPhone(addressBook.getPhone());
        orders.setNumber(String.valueOf(System.currentTimeMillis()));
        orders.setUserId(userId);
        orders.setConsignee(addressBook.getConsignee());
        orders.setAddress(addressBook.getDetail());
        orderMapper.insert(orders);

        // 2. 向order_detail表插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : list) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtil.copyProperties(cart, orderDetail);
            // 关联的订单id
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);

        // 3. 清空shopping_cart
        shoppingCartMapper.deleteByBatch(ShoppingCart.builder().userId(userId).build());

        // 4. 封装VO对象
        return OrderSubmitVO.builder().id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();
    }

    @Override
    @Transactional
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userMapper.getById(userId);

        //调用微信支付接口，生成预支付交易单
/*        JSONObject jsonObject = weChatPayUtil.pay(
                ordersPaymentDTO.getOrderNumber(), //商户订单号
                new BigDecimal(0.01), //支付金额，单位 元
                "苍穹外卖订单", //商品描述
                user.getOpenid() //微信用户的openid
        );

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }*/
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", "ORDERPAID");
        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        // 微信支付的数据库订单状态更新
        Orders orders = Orders.builder().number(ordersPaymentDTO.getOrderNumber())
                .payStatus(Orders.PAID)
                .status(Orders.TO_BE_CONFIRMED)
                .checkoutTime(LocalDateTime.now())
                .build();
        orderMapper.updateByNumber(orders);

        return vo;
    }

    @Override
    @Transactional
    public void paySuccess(String outTradeNo) {
        Long userId = BaseContext.getCurrentId();

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumberAndUserId(outTradeNo, userId);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    @Override
    @Transactional
    public OrderVO getDetail(Long id) {
        // 1. 获取Orders
        Orders orders = orderMapper.getById(id);

        // 2. 获取OrderDetail
        List<OrderDetail> list = orderDetailMapper.getByOrderId(id);

        OrderVO orderVO = new OrderVO();
        BeanUtil.copyProperties(orders, orderVO);
        orderVO.setOrderDetailList(list);
        return orderVO;
    }

    @Override
    @Transactional
    public PageResult history(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());
        Orders query = Orders.builder()
                .userId(BaseContext.getCurrentId())
                .status(ordersPageQueryDTO.getStatus())
                .build();
        Page<OrderVO> ordersPage = orderMapper.history(query);
        long total = ordersPage.getTotal();
        List<OrderVO> result = ordersPage.getResult();
        for (OrderVO vo : result) {
            vo.setOrderDetailList(orderDetailMapper.getByOrderId(vo.getId()));
        }
        return new PageResult(total, result);
    }

    @Override
    @Transactional
    public PageResult search(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(), ordersPageQueryDTO.getPageSize());

        Page<OrderVO> ordersPage = orderMapper.search(ordersPageQueryDTO);
        long total = ordersPage.getTotal();
        List<OrderVO> result = ordersPage.getResult();
        for (OrderVO vo : result) {
            List<OrderDetail> list = orderDetailMapper.getByOrderId(vo.getId());
            String orderDishes = list.stream().map(OrderDetail::getName).collect(Collectors.joining(","));
            vo.setOrderDishes(orderDishes);
        }

        return new PageResult(total, result);
    }

    @Override
    public OrderStatisticsVO statistics() {
        return orderMapper.statistics();
    }

    @Override
    @Transactional
    public void cancel(Long id, String cancelReason) {
        // 1. 查询订单
        Orders orders = orderMapper.getById(id);
        // 应当再加上检测功能，但是如果是正常通过小程序获取则不需要

        // 2. 设置订单状态
        orders.setStatus(Orders.CANCELLED);
        // 3. 设置取消原因
        orders.setCancelReason(cancelReason);
        // 4. 设置取消时间
        orders.setCancelTime(LocalDateTime.now());

        // 5. 更新
        orderMapper.update(orders);
    }

    @Override
    @Transactional
    public void repetition(Long id) {
        // 1. 获取旧订单
        Orders orders = orderMapper.getById(id);
        // 2. 获取旧订单对应的所有菜品
        Long userId = BaseContext.getCurrentId();
        Long orderId = orders.getId();
        List<OrderDetail> list = orderDetailMapper.getByOrderId(orderId);

        // 3. 将菜品加入购物车
        List<ShoppingCart> shoppingCarts = new ArrayList<>();
        for (OrderDetail each : list) {
            ShoppingCart cart = ShoppingCart.builder()
                    .userId(userId)
                    .dishId(each.getDishId())
                    .dishFlavor(each.getDishFlavor())
                    .setmealId(each.getSetmealId())
                    .image(each.getImage())
                    .name(each.getName())
                    .amount(each.getAmount())
                    .number(each.getNumber())
                    .createTime(LocalDateTime.now())
                    .build();
            shoppingCarts.add(cart);
        }
        shoppingCartMapper.insertBatch(shoppingCarts);
    }
}
