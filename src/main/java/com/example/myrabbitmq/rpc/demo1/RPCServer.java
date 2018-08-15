package com.example.myrabbitmq.rpc.demo1;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeoutException;

/**
 * rpc服务端
 * @author by Joney on 2018/8/8 18:20
 */
public class RPCServer {

    private static final String RPC_QUEUE_NAME = "rpc_queue";

    public static void main(String[] args) {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.56.101");
        factory.setPort(5672);
        factory.setUsername("xiawq");
        factory.setPassword("123456");

        Connection connection = null;

        try {
            connection = factory.newConnection();
            final Channel channel = connection.createChannel();

            // 声明队列
            channel.queueDeclare(RPC_QUEUE_NAME ,false, false, false, null);
            channel.basicQos(1);
            System.out.println(" [x] Awaiting RPC requests");

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(properties.getCorrelationId())
                            .build();
                    String response = "";

                    try {
                        String message = new String(body, "UTF-8");
                        int n =Integer.parseInt(message);

                        System.out.println(" [.] fib(" + message + ")");
                        response += fib(n);
                    } catch (RuntimeException e) {
                        System.out.println(" [.] " + e.toString());
                    } finally {
                        // 使用客户端回调队列（即replyTo字段）将带有结果的消息发送回客户端
                        channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
                        channel.basicAck(envelope.getDeliveryTag(), false);

                        // Rabbitmq consumer工作线程唤醒RPC服务器所有者线程
                        synchronized (this) {
                            this.notify();
                        }
                    }
                }
            };
            channel.basicConsume(RPC_QUEUE_NAME, false, consumer);

            // 线程等待，准备消费来自客户端的消息
            while(true) {
                synchronized (consumer) {
                    try {
                        consumer.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static int fib(int n) {
        if (n == 0) return 0;
        if (n == 1) return 1;
        return fib(n-1) + fib(n-2);
    }
}
