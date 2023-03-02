package com.tian.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Map;

@Repository
public class StockDaoImpl implements IStockDao {

    @Autowired
    public JdbcTemplate jdbcTemplate;

    //查询全部商品
    public ArrayList<Map<String, Object>> getStockList() {
        //1.创建商品SQL
        String sql = "select id as sku_id, title, images, stock, price, indexes, own_spec " +
                "from tb_sku";

        //2.执行SQL语句
        ArrayList<Map<String, Object>> list = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql);

        //3.返回信息
        return list;
    }

    //查看商品详情
    public ArrayList<Map<String, Object>> getStock(int sku_id) {

        //1.创建商品查询的SQL
        String sql = "select tb_sku.spu_id, tb_sku.title, tb_sku.images, tb_sku.stock, tb_sku.price, tb_sku.indexes, " +
                "tb_sku.own_spec, tb_sku.enable, tb_sku.create_time, tb_sku.update_time, tb_spu_detail.description, " +
                "tb_sku.id as sku_id, tb_spu_detail.special_spec " +
                "from tb_sku " +
                "inner join tb_spu_detail on tb_sku.spu_id=tb_spu_detail.spu_id " +
                "where tb_sku.id=?";

        //2.执行SQL语句
        ArrayList<Map<String, Object>> list = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql, sku_id);

        //3.返回信息
        return list;
    }

    //商品政策写入
    public boolean insertLimitPolicy(Map<String, Object> policyInfo) {

        //1.创建写入政策的SQL语句
        String sql = "insert into tb_limit_policy (sku_id, quanty, price, begin_time, end_time) " +
                "values (?, ?, ?, ?, ?)";

        //2.执行SQL语句
        boolean result = jdbcTemplate.update(sql, policyInfo.get("sku_id"), policyInfo.get("quanty"), policyInfo.get("price"),
                policyInfo.get("begin_time"), policyInfo.get("end_time")) == 1;

        //3.返回信息
        return result;

    }


}