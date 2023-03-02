package com.tian.service;

import java.util.Map;

public interface IOrderService {

    //创建订单
    Map<String, Object> createOrder(int sku_id, int user_id);

    //写入订单
    Map<String, Object> insertOrder(String order);

    //查询订单
    Map<String, Object> getOrder(int order_id);

    //更新订单
    Map<String, Object> updateOrder(int order_id);

    //支付订单
    Map<String, Object> payOrder(int order_id, int sku_id);

}
