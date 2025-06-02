package com.sky.mapper;

import com.github.pagehelper.Page;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Orders;
import com.sky.vo.OrderStatisticsVO;
import com.sky.vo.OrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface OrderMapper {
    void insert(Orders orders);

    @Select("select * from orders where number = #{orderNumber}")
    Orders getByNumber(String orderNumber);

    @Select("select * from orders where number = #{orderNumber} and user_id= #{userId}")
    Orders getByNumberAndUserId(String orderNumber, Long userId);

    void update(Orders orders);

    void updateByNumber(Orders orders);

    @Select("select * from orders where id = #{id}")
    Orders getById(Long id);

    Page<OrderVO> history(Orders query);

    Page<OrderVO> search(OrdersPageQueryDTO query);

    OrderStatisticsVO statistics();
}
