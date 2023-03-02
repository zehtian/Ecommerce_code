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
import java.util.Map;

@Repository
public class UserDaoImpl implements IUserDao{

    @Autowired
    private JdbcTemplate jdbcTemplate;

    //查找一个用户
    //从数据库中查询会员信息
    public ArrayList<Map<String, Object>> getUser(String username, String password){
        //1.创建sql
        String sql = "select id as user_id, username, password, phone from tb_user where username=?";

        //2.执行sql
        ArrayList<Map<String, Object>> list = (ArrayList<Map<String, Object>>) jdbcTemplate.queryForList(sql, username);

        //3.返回数据
        return list;
    }

    //添加一个用户
    public int insertUser(String username, String password){
        //1.创建sql
        final String sql = "insert into tb_user (username, phone, password) " +
                "values ('"+username+"', '"+username+"', '"+password+"')";

        //2.执行sql，并且获取id
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(new PreparedStatementCreator() {
            public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
                return connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            }
        }, keyHolder);

        //3.返回数据
        return keyHolder.getKey().intValue();
    }

}
