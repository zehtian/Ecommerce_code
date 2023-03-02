package com.tian.queue;

import com.tian.service.IOrderService;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class OrderQueue {

    @Autowired
    private IOrderService iOrderService;

    //进行order_queue队列的监听
    @RabbitListener(queues = "order_queue")
    public void insertQueue(String msg){
        //1.接收到消息
        System.out.println("order_queue接收消息"+msg);

        //2.执行insertOrder方法，传回来的消息msg就是order
        Map<String, Object> resultMap = iOrderService.insertOrder(msg);

        //3.如果失败，输出失败信息
        if(!(Boolean) resultMap.get("result")){
            System.out.println("order_queue处理消息失败："+resultMap.get("msg").toString());
        }
        else{
            //4.输出成功信息
            System.out.println("order_queue处理消息成功！");
        }
    }


    //订单更新状态队列
    @RabbitListener(queues = "order_status_queue")
    public void updateOrderStatus(int msg){
        //1.接收到消息
        System.out.println("order_status_queue接收消息"+msg);

        //2.执行updateOrder方法，传回来的消息msg就是order_id
        Map<String, Object> resultMap = iOrderService.updateOrder(msg);

        //3.如果失败，输出失败信息
        if(!(Boolean) resultMap.get("result")){
            System.out.println("order_status_queue处理消息失败！");
        }
        else{
            //4.输出成功信息
            System.out.println("order_status_queue处理消息成功！");
        }
    }
}
