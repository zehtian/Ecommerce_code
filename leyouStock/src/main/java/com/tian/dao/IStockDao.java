package com.tian.dao;

import java.util.ArrayList;
import java.util.Map;

public interface IStockDao {
    //查询全部商品
    ArrayList<Map<String, Object>> getStockList();

    //查看商品详情
    ArrayList<Map<String, Object>> getStock(int sku_id);

    //商品政策写入
    boolean insertLimitPolicy(Map<String, Object> policyInfo);

}
