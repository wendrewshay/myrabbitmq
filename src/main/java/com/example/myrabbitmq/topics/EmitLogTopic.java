package com.example.myrabbitmq.topics;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * 主题消息发布：topic类型的交换器
 * @author by Joney on 2018/8/8 17:37
 */
public class EmitLogTopic {

    private static final String EXCHANGE_NAME = "topic_logs";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.56.101");
        factory.setPort(5672);
        factory.setUsername("xiawq");
        factory.setPassword("123456");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "topic"); // 声明topic类型交换器
        List<String> routingKeys = Arrays.asList("com.my.apple", "com.red.hat", "org.rabbit.grass", "spring.other.frame"); // 用来测试的路由键
        for (String routingKey : routingKeys) {
            String message = "我的消息 - '" + routingKey +"'";
            channel.basicPublish(EXCHANGE_NAME, routingKey, null, message.getBytes());
            System.out.println(" [x] Sent '" + routingKey + "':'" + message + "'");
            Thread.sleep(1000);
        }

        channel.close();
        connection.close();
    }
}
