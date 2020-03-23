/**
 * @program school-bus
 * @description: OrderAddCancleListener
 * @author: mf
 * @create: 2020/03/19 18:00
 */

package com.stylefeng.guns.rest.modular.order.mq;

import com.alibaba.fastjson.JSON;
import com.stylefeng.guns.core.constants.MqTags;
import com.stylefeng.guns.rest.mq.MQDto;
import com.stylefeng.guns.rest.order.IOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.spring.annotation.MessageModel;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

@Slf4j
@Component
@RocketMQMessageListener(topic = "${mq.order.topic}", consumerGroup = "${mq.order.consumer.group.name}",messageModel = MessageModel.BROADCASTING)
public class OrderAddCancleListener implements RocketMQListener<MessageExt> {

    @Autowired
    private IOrderService orderService;

    @Override
    public void onMessage(MessageExt messageExt) {
        try {
            // 1. 解析消息
            String tags = messageExt.getTags();
            if (!tags.equals(MqTags.ORDER_ADD_CANCLE.getTag())) {
                return;
            }
            String body = new String(messageExt.getBody(), "UTF-8");
            log.warn("收到订单服务异常：" + body);
            MQDto mqDto = JSON.parseObject(body, MQDto.class);
            if (mqDto.getOrderId() != null) {
                // 2. 程序异常或者系统内部异常导致的订单，因此我认为删除该订单。
                // 该订单有可能没有插入成功程序就异常了。
                orderService.deleteOrderById(mqDto.getOrderId());
                log.warn("异常订单已删除");
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            log.error("订单消费信息程序崩...");
        }
    }
}
