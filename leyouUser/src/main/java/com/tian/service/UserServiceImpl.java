package com.tian.service;

import com.tian.dao.IUserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class UserServiceImpl implements IUserService{

    @Autowired
    private IUserDao iUserDao;

    //从数据库中查询会员信息
    public Map<String, Object> getUser(String username, String password) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        //1.判断传入参数
        if(username==null || "".equals(username)) {
            resultMap.put("result", false);
            resultMap.put("msg", "用户名不能为空！");
            return resultMap;
        }

        //2.取自userDao层
        ArrayList<Map<String, Object>> list = iUserDao.getUser(username, password);

        //3.如果没有取出来，返回错误信息
        if(list==null ||list.size()==0){
            resultMap.put("result", false);
            resultMap.put("msg", "没有找到哦会员信息！");
            return resultMap;
        }

        //4.返回正常信息
        resultMap = list.get(0);  //如果查到了，list中就一个用户信息
        resultMap.put("result", true);
        resultMap.put("msg", "");
        return resultMap;
    }


    //添加会员信息
    public Map<String, Object> insertUser(String username, String password){
        Map<String, Object> resultMap = new HashMap<String, Object>();

        //1.判断传入参数
        if(username==null || "".equals(username)) {
            resultMap.put("result", false);
            resultMap.put("msg", "用户名不能为空！");
            return resultMap;
        }

        //2.取自userDao层
        int user_id = iUserDao.insertUser(username, password);

        //3.如果没有执行成功，返回错误信息
        if(user_id<=0){
            resultMap.put("result", false);
            resultMap.put("msg", "数据库没有执行成功！");
            return resultMap;
        }

        //4.返回正常信息
        //保证 insert的resultMap 和 select的resultMap 结构一样，便于登录
        resultMap.put("user_id", user_id);
        resultMap.put("username", username);
        resultMap.put("phone", username);
        resultMap.put("password", password);
        resultMap.put("result", true);
        resultMap.put("msg", "");
        return resultMap;
    }



}
