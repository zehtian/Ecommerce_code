package com.tian.controller;

import com.tian.service.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class OrderController {

    @Autowired
    private IOrderService iOrderService;

    @RequestMapping("/createOrder/{sku_id}/{user_id}")
    public Map<String, Object> createOrder(@PathVariable("sku_id") int sku_id, @PathVariable("user_id") int user_id){
        return iOrderService.createOrder(sku_id, user_id);
    }


    @RequestMapping("/getOrder/{order_id}")
    public Map<String, Object> getOrder(@PathVariable("order_id") int order_id){
        return iOrderService.getOrder(order_id);
    }


    @RequestMapping("/payOrder/{order_id}/{sku_id}")
    public Map<String, Object> payOrder(@PathVariable("order_id") int order_id, @PathVariable("sku_id") int sku_id){
        Map<String, Object> resultMap = new HashMap<String, Object>();

        //判断订单状态是否满足要求，即订单是否支付
        //这里默认成功支付了，实际上是由外部接口返回（如：支付宝，微信...）
        int isPay = 1;

        if(isPay==0){
            resultMap.put("result", false);
            resultMap.put("msg", "订单未成功支付！");
            return resultMap;
        }

        return iOrderService.payOrder(order_id, sku_id);
    }
}
