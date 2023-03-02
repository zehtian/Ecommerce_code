package com.tian.service;

import java.util.Map;

public interface IUserService {

    //从数据库中查询会员信息
    Map<String, Object> getUser(String username, String password);

    //添加会员信息
    Map<String, Object> insertUser(String username, String password);


}
