package com.example.myrabbitmq.workqueues;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 发布消息到指定命名的工作队列
 * @author by Joney on 2018/8/8 12:24
 */
public class NewTask {

    private final static String QUEUE_NAME = "task_queue";

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.56.101");
        factory.setPort(5672);
        factory.setUsername("xiawq");
        factory.setPassword("123456");
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        boolean durable = true ; // 声明队列是持久的，即使重启rabbitmq，队列也不会丢失
        channel.queueDeclare(QUEUE_NAME, durable, false, false, null);

//        String message = getMessage(args);

        for (int i = 0; i < 100000; i ++) {
            String message = "我的消息[" + i +"]";
            // 将消息标记为持久性，通过将MessageProperties（实现BasicProperties）设置为值PERSISTENT_TEXT_PLAIN。
            channel.basicPublish("", QUEUE_NAME, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));
            System.out.println(" [x] Sent '" + message + "'");
//            Thread.sleep(1000);
        }


        channel.close();
        connection.close();
    }

    private static String getMessage(String[] strings) {
        if(strings.length < 1) {
            return "Hello World";
        } else {
            return joinStrings(strings, " ");
        }
    }

    private static String joinStrings(String[] strings, String delimiter) {
        int length = strings.length;
        if (length == 0) return "";

        StringBuilder words = new StringBuilder(strings[0]);
        for (int i = 1; i < length; i ++) {
            words.append(delimiter).append(strings[i]);
        }
        return words.toString();
    }
}
