package com.tian.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sun.xml.internal.ws.policy.PolicyMap;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class OrderServiceImpl implements IOrderService{

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private AmqpTemplate amqpTemplate;


    //创建订单
    public Map<String, Object> createOrder(int sku_id, int user_id){
        Map<String, Object> resultMap = new HashMap<String, Object>();

        //1.判断传入的参数
        if(sku_id==0 || user_id==0){
            resultMap.put("result", false);
            resultMap.put("msg", "前端传入参数有误！");
            return resultMap;
        }

        //用系统时间来作为不同订单的id值，来保证每一个订单id不一样
        int order_id = (int) System.currentTimeMillis();

        //2.取redis政策
        String policy = stringRedisTemplate.opsForValue().get("LIMIT_POLICY_" + sku_id);

        if(policy!=null && !"".equals(policy)){
            //3.判断时间，开始时间<=当前时间<=结束时间
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            //政策转化为Map，便于读取
            Map<String, Object> policyInfo = JSONObject.parseObject(policy, Map.class);

            String now = restTemplate.getForObject("http://leyou-time-server/getTime", String.class);
            try {
                Date begin_time = simpleDateFormat.parse(policyInfo.get("begin_time").toString());
                Date end_time = simpleDateFormat.parse(policyInfo.get("end_time").toString());
                Date now_time = simpleDateFormat.parse(now);

                if(begin_time.getTime()<=now_time.getTime() && now_time.getTime()<=end_time.getTime()){
                    //取出秒杀政策中的库存数量
                    long limitQuanty = Long.parseLong(policyInfo.get("quanty").toString());

                    //4.redis计数器
                    //计数器，每次进来一个，数量加一
                    if(stringRedisTemplate.opsForValue().increment("SKU_QUANTY_"+sku_id, 1)<=limitQuanty){
                        //5.通过计数器的部分，写入订单队列，并且写入redis
                        Map<String, Object> orderInfo = new HashMap<String, Object>();
                        //从redis中取商品信息
                        String sku = stringRedisTemplate.opsForValue().get("SKU_" + sku_id);
                        Map<String, Object> skuMap = JSONObject.parseObject(sku, Map.class);

                        orderInfo.put("order_id", order_id);
                        orderInfo.put("total_fee", skuMap.get("price"));
                        orderInfo.put("actual_fee", policyInfo.get("price"));
                        orderInfo.put("post_fee", 0);
                        orderInfo.put("payment_type", 0);
                        orderInfo.put("user_id", user_id);
                        orderInfo.put("status", 1);
                        orderInfo.put("create_time", now);

                        orderInfo.put("sku_id", skuMap.get("sku_id"));
                        orderInfo.put("num", 1);
                        orderInfo.put("title", skuMap.get("title"));
                        orderInfo.put("own_spec", skuMap.get("own_spec"));
                        orderInfo.put("price", policyInfo.get("price"));
                        orderInfo.put("images", skuMap.get("images"));

                        try {
                            String order = JSON.toJSONString(orderInfo);
                            //写入订单队列
                            //进行order_queue队列的写入
                            amqpTemplate.convertAndSend("order_queue", order);
                            //将订单存到redis中
                            stringRedisTemplate.opsForValue().set("ORDER_"+order_id, order);
                        } catch (Exception e) {
                            resultMap.put("result", false);
                            resultMap.put("msg", "队列写入失败！");
                            e.printStackTrace();
                        }
                    }
                    else{
                        //6.没有通过计数器，提示商品已经售完
                        resultMap.put("result", false);
                        resultMap.put("msg", "商品已经售完，踢回去的3亿9！");
                        return resultMap;
                    }
                }
                else {
                    //7.时间判断以外，提示活动已经过期
                    resultMap.put("result", false);
                    resultMap.put("msg", "活动已经过期！");
                    return resultMap;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        else{
            //8.没有取出政策，提示活动已过期
            resultMap.put("result", false);
            resultMap.put("msg", "活动已经过期！");
            return resultMap;
        }

        //9.返回正常信息，包含order_id
        resultMap.put("order_id", order_id);
        resultMap.put("result", true);
        resultMap.put("msg", "");
        return resultMap;
    }


    //写入订单
    public Map<String, Object> insertOrder(String order){
        //根据订单队列的监听，一旦用户创建完订单，将进入队列的订单信息写入数据库
        Map<String, Object> resultMap = new HashMap<String, Object>();

        //1.判断传入的参数
        if(order==null || "".equals(order)){
            resultMap.put("result", false);
            resultMap.put("msg", "订单信息为空！");
            return resultMap;
        }

        Map<String, Object> orderInfo = JSONObject.parseObject(order, Map.class);

        //2.编写sql 随便写几个属性
        String sql = "insert into tb_order (order_id, total_fee, actual_fee, post_fee, payment_type, user_id, status, create_time, " +
                "sku_id, num, title, own_spec, price, images) " +
                "values ('"+orderInfo.get("order_id")+"', '"+orderInfo.get("total_fee")+"', '"+orderInfo.get("actual_fee")+
                "', '"+orderInfo.get("post_fee")+"', '"+orderInfo.get("payment_type")+"', '"+orderInfo.get("user_id")+
                "', '"+orderInfo.get("status")+"', '"+orderInfo.get("create_time")+"', '"+orderInfo.get("sku_id")+
                "', '"+orderInfo.get("num")+"', '"+orderInfo.get("title")+"', '"+orderInfo.get("own_spec")+
                "', '"+orderInfo.get("price")+"', '"+orderInfo.get("images")+"')";

        //3.执行sql，判断是否写入成功
        boolean result = jdbcTemplate.update(sql)==1;

        if(!result){
            //4.写入失败，返回错误信息
            resultMap.put("result", false);
            resultMap.put("msg", "数据库写入订单失败！");
            return resultMap;
        }

        //5.返回正常信息
        resultMap.put("result", true);
        resultMap.put("msg", "");
        return resultMap;
    }


    //查询订单信息-从redis取
    public Map<String, Object> getOrder(int order_id){
        Map<String, Object> resultMap = new HashMap<String, Object>();

        //1.判断传入的参数
        if(order_id==0){
            resultMap.put("result", false);
            resultMap.put("msg", "订单有误！");
            return resultMap;
        }

        //2.从redis中取出订单
        String order = stringRedisTemplate.opsForValue().get("ORDER_" + order_id);
        if(order==null ||"".equals(order)){
            //3.未有效取出订单
            resultMap.put("result", false);
            resultMap.put("msg", "查询订单失败！");
            return resultMap;
        }

        //4.成功取出订单，返回正常值
        Map<String, Object> orderInfo = JSONObject.parseObject(order, Map.class);
        resultMap.put("order", orderInfo);
        resultMap.put("result", true);
        resultMap.put("msg", "");

        return resultMap;
    }


    //更新订单信息
    public Map<String, Object> updateOrder(int order_id) {
        //根据订单状态队列的监听，一旦用户完成支付，进行订单状态的更新
        Map<String, Object> resultMap = new HashMap<String, Object>();

        //1.判断传入的参数
        if(order_id==0){
            resultMap.put("result", false);
            resultMap.put("msg", "订单有误！");
            return resultMap;
        }

        //2.编写sql，更新订单状态
        String sql = "update tb_order set status=0 where order_id=?";

        //3.执行sql，判断是否写入成功
        boolean result = jdbcTemplate.update(sql, order_id)==1;

        if(!result){
            //4.状态更新失败，返回错误信息
            resultMap.put("result", false);
            resultMap.put("msg", "数据库订单状态更新失败！");
            return resultMap;
        }

        //5.返回正常信息
        resultMap.put("result", true);
        resultMap.put("msg", "");
        return resultMap;
    }


    //订单支付
    public Map<String, Object> payOrder(int order_id, int sku_id){
        Map<String, Object> resultMap = new HashMap<String, Object>();

        //1.判断传入的参数
        if(order_id==0){
            resultMap.put("result", false);
            resultMap.put("msg", "订单有误！");
            return resultMap;
        }

        //2.写入队列
        try {
            //写入订单状态队列，表示订单已经支付
            amqpTemplate.convertAndSend("order_status_queue", order_id);
            //写入库存队列，提示库存需要对应减一
            amqpTemplate.convertAndSend("storage_queue", sku_id);
        } catch (Exception e) {
            resultMap.put("result", false);
            resultMap.put("msg", "队列写入失败！");
            return resultMap;
        }

        //3.返回正常信息
        resultMap.put("result", true);
        resultMap.put("msg", "");
        return resultMap;

    }




}
