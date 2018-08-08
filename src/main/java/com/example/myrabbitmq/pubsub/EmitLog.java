package com.example.myrabbitmq.pubsub;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 发布
 * fanout类型交换器会均匀分发消息到各个客户端队列
 * @author by Joney on 2018/8/8 15:58
 */
public class EmitLog {

    private static final String EXCHANGE_NAME = "logs";

    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.56.101");
        factory.setPort(5672);
        factory.setUsername("xiawq");
        factory.setPassword("123456");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout"); // 声明交换器

        String message = "Hello World";
        channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
        System.out.printf(" [x] Sent '" + message + "'");

        channel.close();
        connection.close();
    }
}
