package com.tian.controller;

import com.alibaba.fastjson.JSONObject;
import com.tian.service.IStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class StockController {

    @Autowired
    private IStockService iStockService;

    @RequestMapping(value = "/getStockList")
    public Map<String, Object> getStockList(){
        return iStockService.getStockList();
    }

    @RequestMapping(value = "/getStock/{sku_id}")
    public Map<String, Object> getStock(@PathVariable("sku_id") int sku_id){
        return iStockService.getStock(sku_id);
    }

    @RequestMapping(value = "/insertLimitPolicy/{json}")
    //示例：http://localhost:7000/insertLimitPolicy/{sku_id:2600242,quanty:1000,price:1000,begin_time:'2023-02-21 11:00',end_time:'2023-03-01 12:00'}
    public Map<String, Object> insertLimitPolicy(@PathVariable("json") String json) {
        //前端传入的是json字符串，需要将其转化为Map
        Map<String, Object> policyInfo = JSONObject.parseObject(json, Map.class);
        return iStockService.insertLimitPolicy(policyInfo);

    }
}
