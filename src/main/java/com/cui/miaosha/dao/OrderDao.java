package com.cui.miaosha.dao;

import com.cui.miaosha.domain.MiaoshaOrder;
import com.cui.miaosha.domain.OrderInfo;
import com.cui.miaosha.domain.User;
import com.cui.miaosha.vo.GoodsVo;
import org.apache.ibatis.annotations.*;

@Mapper
public interface OrderDao {
    @Select("select * from miaosha_order where user_id=#{userId} and goods_id=#{goodsId}")
    MiaoshaOrder getMiaoshaOrderByUserIdGoodsId(@Param("userId") Long userId, @Param("goodsId") Integer goodsId);

    @Insert("insert into order_info (user_id, goods_id, delivery_addr_id, goods_name, goods_count, goods_price, order_channel, order_status, create_date, pay_date) " +
            "values (#{userId},#{goodsId},#{deliveryAddrId},#{goodsName},#{goodsCount},#{goodsPrice},#{orderChannel},#{orderStatus},#{createDate},#{payDate})")
    @SelectKey(keyColumn = "id",keyProperty = "id",resultType = Integer.class,before = false,statement = "select last_insert_id()")
    Integer insert(OrderInfo orderInfo);

    @Insert("insert into miaosha_order (user_id, order_id, goods_id) " +
            "values (#{userId},#{orderId},#{goodsId})")
    Integer insertMiaoshaOrder(MiaoshaOrder miaoshaOrder);

    @Select("select * from order_info where id=#{id}")
    OrderInfo getOrderById(@Param("id") Integer orderId);
}
