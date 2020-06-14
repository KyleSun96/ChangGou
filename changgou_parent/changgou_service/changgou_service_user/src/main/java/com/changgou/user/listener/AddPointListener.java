package com.changgou.user.listener;

import com.alibaba.fastjson.JSON;
import com.changgou.order.pojo.Task;
import com.changgou.user.config.RabbitMQConfig;
import com.changgou.user.service.UserService;
import org.apache.commons.lang.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * @Program: ChangGou
 * @ClassName: AddPointListener
 * @Description: 接收消息监听类
 * @Author: KyleSun
 **/
@Component
public class AddPointListener {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private UserService userService;


    @RabbitListener(queues = RabbitMQConfig.CG_BUYING_ADDPOINT)
    public void receiveAddPointMessage(String message) {

        System.out.println("3.用户服务接收到了任务消息");

        // 转换消息
        Task task = JSON.parseObject(message, Task.class);
        if (task == null || StringUtils.isEmpty(task.getRequestBody())) {
            // 如果task为空，方法结束
            return;
        }

        // 判断redis中当前的任务是否存在
        Object value = redisTemplate.boundValueOps(task.getId()).get();
        if (value != null) {
            // 如果redis中有内容，代表任务正在执行中，方法结束
            return;
        }

        // 更新用户积分
        int count = userService.updateUserPoint(task);
        if (count == 0) {
            // 影响0行，操作失败
            return;
        }

        // 向订单服务返回通知消息
        rabbitTemplate.convertAndSend(RabbitMQConfig.EX_BUYING_ADDPOINTUSER, RabbitMQConfig.CG_BUYING_FINISHADDPOINT_KEY, JSON.toJSONString(task));
        System.out.println("6.用户服务向完成添加积分队列发送了一条消息");

    }


}
