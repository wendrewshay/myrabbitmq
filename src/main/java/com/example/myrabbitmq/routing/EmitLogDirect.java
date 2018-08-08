package com.example.myrabbitmq.routing;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;

/**
 * 路由发送：direct类型交换器
 * 需要配置routingKey(路由键)，direct类型交换器可以根据订阅的routingKey推送消息到客户端队列
 * @author by Joney on 2018/8/8 17:00
 */
public class EmitLogDirect {

    private static final String EXCHANGE_NAME = "direct_logs";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.56.101");
        factory.setPort(5672);
        factory.setUsername("xiawq");
        factory.setPassword("123456");

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, "direct");

        List<String> severitys = Arrays.asList("info", "debug", "error", "warn");
        for (String severity : severitys) {
            String message = "[" + severity + "] - 我的消息";
            // severity即路由键
            channel.basicPublish(EXCHANGE_NAME, severity, null, message.getBytes());
            System.out.println(" [x] Sent '" + severity + "':'" + message + "'");
            Thread.sleep(1000);
        }

        channel.close();
        connection.close();
    }
}
