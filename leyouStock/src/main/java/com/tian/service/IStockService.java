package com.tian.service;

import java.util.Map;

public interface IStockService {

    //查询全部商品
    Map<String, Object> getStockList();

    //查看商品详情
    Map<String, Object> getStock(int sku_id);

    //商品政策写入
    Map<String, Object> insertLimitPolicy(Map<String, Object> policyInfo);

}
