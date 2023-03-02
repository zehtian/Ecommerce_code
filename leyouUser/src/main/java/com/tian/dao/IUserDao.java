package com.tian.dao;

import java.util.ArrayList;
import java.util.Map;

public interface IUserDao {

    //从数据库中查询会员信息
    ArrayList<Map<String, Object>> getUser(String username, String password);

    //添加会员信息
    int insertUser(String username, String password);


}
