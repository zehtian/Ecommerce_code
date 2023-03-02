package com.tian.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tian.dao.IStockDao;
import javafx.scene.chart.PieChart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class StockServiceImpl implements IStockService{

    @Autowired
    private IStockDao iStockDao;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //微服务中跨服务器取当前时间，需手动注入bean
    @Autowired
    private RestTemplate restTemplate;


    //查询全部商品（商品首页信息）
    public Map<String, Object> getStockList(){
        //把List再包装成Map传递给前端
        Map<String, Object> resultMap = new HashMap<String, Object>();

        //1.取自iStockDao的方法
        ArrayList<Map<String, Object>> list = iStockDao.getStockList();

        //2.如果没有取出来，返回错误信息
        if(list==null||list.size()==0){
            resultMap.put("result", false);
            resultMap.put("msg", "没有取出商品信息！");
            return resultMap;
        }

        //3.取redis政策（也就是秒杀信息，也应该算在商品信息中，并进行前端显示）
        resultMap = getLimitPolicy(list);

        //4.返回正常信息
        resultMap.put("sku", list);
//        resultMap.put("result", true);
//        resultMap.put("msg", "");

        return resultMap;
    }


    //查看商品详情（商品详情页信息）
    public Map<String, Object> getStock(int sku_id){
        //把List再包装成Map传递给前端
        Map<String, Object> resultMap = new HashMap<String, Object>();

        //1.判断传入的参数
        if(sku_id==0){
            resultMap.put("result", false);
            resultMap.put("msg", "传入的参数有误！");
            return resultMap;
        }

        //2.取自iStockDao的方法
        ArrayList<Map<String, Object>> list = iStockDao.getStock(sku_id);

        //3.如果没有取出来，返回错误信息
        if(list==null||list.size()==0){
            resultMap.put("result", false);
            resultMap.put("msg", "没有取出商品信息！");
            return resultMap;
        }

        //4.取redis政策（也就是秒杀信息，也应该算在商品信息中，并进行前端显示）
        resultMap = getLimitPolicy(list);

        //5.返回正常信息
        resultMap.put("sku", list);
//        resultMap.put("result", true);
//        resultMap.put("msg", "");

        return resultMap;
    }

    //商品政策写入
    public Map<String, Object> insertLimitPolicy(Map<String, Object> policyInfo){
        //把List再包装成Map传递给前端
        Map<String, Object> resultMap = new HashMap<String, Object>();

        //1.判断传入的参数
        if(policyInfo==null||policyInfo.isEmpty()){
            resultMap.put("result", false);
            resultMap.put("msg", "传入的参数有误！");
            return resultMap;
        }

        //2.取自iStockDao的方法
        boolean result = iStockDao.insertLimitPolicy(policyInfo);

        //3.如果插入失败，返回错误信息
        if(!result){
            resultMap.put("result", false);
            resultMap.put("msg", "数据库写入政策时失败！");
            return resultMap;
        }

        //4.写入redis, StringRedisTemplate
        //4.1.取名 key:LIMIT_POLICY_{sku_id}, value:policyInfo
        //4.2.redis有效期 有效期：结束时间-当前时间
        //计算时间
        long diff = 0;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        String now = restTemplate.getForObject("http://leyou-time-server/getTime", String.class);
        try {
            Date end_time = simpleDateFormat.parse(policyInfo.get("end_time").toString());
            Date now_time = simpleDateFormat.parse(now);

            diff = (end_time.getTime()-now_time.getTime())/1000;
            if(diff<=0){
                resultMap.put("result", false);
                resultMap.put("msg", "结束时间不能小于当前时间！");
                return resultMap;
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        //政策存入redis
        String policy = JSON.toJSONString(policyInfo);
        //写入redis缓存, 参数1:名称, 参数2:内容(json), 参数3:缓存有效时间, 有效期一到, 这条缓存就会被物理删除, 参数4:计时方式
        stringRedisTemplate.opsForValue().set("LIMIT_POLICY_"+policyInfo.get("sku_id").toString(), policy, diff, TimeUnit.SECONDS);

        //商品存入redis
        ArrayList<Map<String, Object>> list = iStockDao.getStock((Integer) policyInfo.get("sku_id"));
        String sku = JSON.toJSONString(list.get(0));  //list中就一个, 直接取出来
        stringRedisTemplate.opsForValue().set("SKU_"+policyInfo.get("sku_id").toString(), sku, diff, TimeUnit.SECONDS);

        //5.返回正常信息
        resultMap.put("result", true);
        resultMap.put("msg", "数据库政策写入完毕！");
        return resultMap;
    }


    //取redis政策（也就是秒杀信息，也应该算在商品信息中，并进行前端显示）
    private Map<String, Object> getLimitPolicy(ArrayList<Map<String, Object>> list){

        Map<String, Object> resultMap = new HashMap<String, Object>();

        //取redis政策（也就是秒杀信息，也应该算在商品信息中，并进行前端显示）
        for(Map<String, Object> skuMap:list){

            //4.1.取政策，如果取到了政策，才给商品赋值
            String policy = stringRedisTemplate.opsForValue().get("LIMIT_POLICY_" + skuMap.get("sku_id").toString());
            if(policy!=null && !policy.equals("")) {

                //存在redis的值是json格式，要传化为Map进行操作
                Map<String, Object> policyInfo = JSONObject.parseObject(policy, Map.class);

                //4.2.开始时间必须小于等于当前时间，当前时间小于等于结束时间
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String now = restTemplate.getForObject("http://leyou-time-server/getTime", String.class);
                try {
                    Date begin_time = simpleDateFormat.parse(policyInfo.get("begin_time").toString());
                    Date end_time = simpleDateFormat.parse(policyInfo.get("end_time").toString());
                    Date now_time = simpleDateFormat.parse(now);

                    //时间满足要求，给商品进行政策的添加赋值，并显示
                    if(begin_time.getTime()<=now_time.getTime() && now_time.getTime()<=end_time.getTime()){
                        //赋值: limitPrice limitQuanty limitBeginTime limitEndTime nowTime
                        skuMap.put("limitPrice", policyInfo.get("price"));
                        skuMap.put("limitQuanty", policyInfo.get("quanty"));
                        skuMap.put("limitBeginTime", policyInfo.get("begin_time"));
                        skuMap.put("limitEndTime", policyInfo.get("end_time"));
                        skuMap.put("nowTime", now);
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
        resultMap.put("result", true);
        resultMap.put("msg", "");

        return resultMap;
    }

}
