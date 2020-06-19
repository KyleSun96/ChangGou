package com.changgou.seckill.service.impl;

import com.alibaba.fastjson.JSON;
import com.changgou.seckill.config.ConfirmMessageSender;
import com.changgou.seckill.config.RabbitMQConfig;
import com.changgou.seckill.pojo.SeckillGoods;
import com.changgou.seckill.pojo.SeckillOrder;
import com.changgou.seckill.service.SecKillOrderService;
import com.changgou.util.IdWorker;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Program: ChangGou
 * @ClassName: SecKillOrderServiceImpl
 * @Description:
 * @Author: KyleSun
 **/
@Service
public class SecKillOrderServiceImpl implements SecKillOrderService {

    public static final String SECKILL_GOODS_KEY = "seckill_goods_";

    public static final String SECKILL_GOODS_STOCK_COUNT_KEY = "seckill_goods_stock_count_";

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private ConfirmMessageSender confirmMessageSender;


    /**
     * @description: //TODO 秒杀异步下单
     * @param: [id, time, username]
     * @return: boolean
     * <p>
     * 1.获取redis中的商品信息与库存信息，并进行判断
     * 2.执行redis的预扣减库存操作，并获取扣减之后的库存值
     * 3.如果扣减之后的库存值 <= 0，说明商品已经卖完了，则删除redis中响应的商品信息与库存信息
     * 4.基于mq完成mysql的数据同步，进行异步下单并扣减库存(mysql)
     */
    @Override
    public boolean add(Long id, String time, String username) {

        // 获取商品信息
        SeckillGoods seckillGoods = (SeckillGoods) redisTemplate.boundHashOps(SECKILL_GOODS_KEY + time).get(id);
        // 获取库存信息（在启动类中已经将商品序列化，指定数据接收类型为string，因此此处需要使用string类型来接收商品库存量）
        String redisStock = (String) redisTemplate.opsForValue().get(SECKILL_GOODS_STOCK_COUNT_KEY + id);
        if (StringUtils.isEmpty(redisStock)) {
            return false;
        }
        int stock = Integer.parseInt(redisStock);
        if (seckillGoods == null || stock <= 0) {
            return false;
        }

        /*
            如何保证redis操作的原子性
                1.decrement:减，increment:加
                2.Lua脚本语言
         */
        // 基于redis的原子性，执行redis的预扣减库存，并获取到扣减之后的库存值，防止货物超卖
        Long decrement = redisTemplate.opsForValue().decrement(SECKILL_GOODS_STOCK_COUNT_KEY + id);
        if (decrement <= 0) {
            // 扣减完库存之后，当前商品已经没有库存了
            // 删除redis中的商品信息与库存信息
            redisTemplate.boundHashOps(SECKILL_GOODS_KEY + time).delete(id);
            redisTemplate.delete(SECKILL_GOODS_STOCK_COUNT_KEY + id);
        }

        // 发送消息(保证消息生产者对于消息的不丢失实现)
        // 消息体: 秒杀订单 --> 转为JSONString传递
        SeckillOrder seckillOrder = new SeckillOrder();
        seckillOrder.setId(idWorker.nextId());
        seckillOrder.setSeckillId(id);
        seckillOrder.setMoney(seckillGoods.getCostPrice());
        seckillOrder.setUserId(username);
        seckillOrder.setSellerId(seckillGoods.getSellerId());
        seckillOrder.setCreateTime(new Date());
        seckillOrder.setStatus("0");

        // 发送消息
        confirmMessageSender.sendMessage("", RabbitMQConfig.SECKILL_ORDER_QUEUE, JSON.toJSONString(seckillOrder));

        return true;
    }
}