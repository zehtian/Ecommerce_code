package com.tian.controller;

import com.alibaba.fastjson.JSON;
import com.tian.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@RestController
public class UserController {

    @Autowired
    private IUserService iUserService;

    //进行会员的登录
    @RequestMapping(value = "/login")  //密码不能直接写入，因此用post
    public Map<String, Object> login(String username, String password, HttpServletRequest httpServletRequest){
        //1.取会员
        Map<String, Object> userMap = iUserService.getUser(username, password);

        //2.如果没有取出会员，则添加会员（注册会员）
        if(!(Boolean) userMap.get("result")){
            userMap = iUserService.insertUser(username, password);

            if(!(Boolean) userMap.get("result")){
                return userMap;
            }
        }

        //3.写入session
        HttpSession httpSession = httpServletRequest.getSession();
        String user = JSON.toJSONString(userMap);
        httpSession.setAttribute("user", user);

        //4.返回信息
        return userMap;

    }


}
