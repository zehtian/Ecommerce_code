package com.tian.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class StorageDaoImpl implements IStorageDao{

    @Autowired
    private JdbcTemplate jdbcTemplate;

    //添加库存
    public Map<String, Object> insertStorage(int sku_id, double in_quanty, double out_quanty){
        //1.查询库存主表
        String sql = "select id from tb_stock_storage where sku_id=?";
        ArrayList<Map<String, Object>> list = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql, sku_id);

        //2.判断主表有没有对应id，并获取id（即库存主键）
        //获取id的作用：1)写入历史表；2)反回来更新
        int new_id = 0;
        //thisQuanty为主表上的数量值，即为增加的订单与减少的订单之差
        double thisQuanty = in_quanty - out_quanty;
        //代表操作成功或失败
        boolean result = false;
        //存放最终结果
        Map<String, Object> resultMap = new HashMap<String, Object>();

        if(list!=null && list.size()>0){
            //2.1.有对应id的话，直接获取id
            new_id = Integer.parseInt(list.get(0).get("id").toString());
        }
        else{
            //2.2.没有的话，写入主表，并且获取id
            sql = "insert into tb_stock_storage (warehouse_id, sku_id, quanty) values (1, "+sku_id+", "+thisQuanty+")";
            final String finalSql = sql;

            //keyholder为插入后的主键值
            KeyHolder keyHolder = new GeneratedKeyHolder();
            //查询当前状态，并获取主键值
            result = jdbcTemplate.update(new PreparedStatementCreator() {
                public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                    return connection.prepareStatement(finalSql, Statement.RETURN_GENERATED_KEYS);
                }
            }, keyHolder)==1;

            if (!result){
                resultMap.put("result", false);
                resultMap.put("msg", "写入库存主表时失败！");
                return resultMap;
            }
            new_id = keyHolder.getKey().intValue();
        }

        //3.写入历史表（历史表反映了库存的具体操作过程）
        //stock_storage_id为主表的id，即new_id
        sql = "insert into tb_stock_storage_history (stock_storage_id, in_quanty, out_quanty) " +
                "values (?, ?, ?)";
        //结果为1是添加成功的意思，insert的返回值是int
        result = jdbcTemplate.update(sql, new_id, in_quanty, out_quanty)==1;

        if (!result){
            resultMap.put("result", false);
            resultMap.put("msg", "写入库存历史表时失败！");
            return resultMap;
        }

        //4.判断有的时候，反回来更新（接着2.1）
        if(list!=null && list.size()>0){
            sql = "update tb_stock_storage set quanty=quanty+"+thisQuanty+" where id="+new_id;
            result = jdbcTemplate.update(sql)==1;
        }

        if (!result){
            resultMap.put("result", false);
            resultMap.put("msg", "更新库存主表时失败！");
            return resultMap;
        }

        //5.返回正常信息
        resultMap.put("result", true);
        resultMap.put("msg", "写入库存成功！");
        return resultMap;

    }



}
