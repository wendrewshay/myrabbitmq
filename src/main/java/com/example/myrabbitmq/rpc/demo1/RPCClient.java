package com.example.myrabbitmq.rpc.demo1;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

/**
 * RPC客户端
 * @author by Joney on 2018/8/8 19:46
 */
public class RPCClient {

    private Connection connection;
    private Channel channel;
    private String requestQueueName = "rpc_queue";
    private String replyQueueName;

    public RPCClient() throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("192.168.56.101");
        factory.setPort(5672);
        factory.setUsername("xiawq");
        factory.setPassword("123456");

        connection = factory.newConnection();
        channel = connection.createChannel();
        // 为每一个客户端创建一个回调队列，使用默认生成的队列作为回调队列，该队列是独占的
        replyQueueName = channel.queueDeclare().getQueue();
    }

    public String call(String message) throws IOException, InterruptedException {
        // 为每一个请求生成一个唯一的correlationId ，用于将RPC响应与请求相关联
        final String corrId = UUID.randomUUID().toString();

        AMQP.BasicProperties props = new AMQP.BasicProperties
                .Builder()
                .correlationId(corrId)
                .replyTo(replyQueueName)
                .build();

        // 发送带有回调队列地址的请求到requestQueueName队列
        channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));

        //由于我们的消费者交付处理是在一个单独的线程中进行的，因此我们需要在响应到来之前暂停主线程。使用BlockingQueue是可能的解决方案之一
        final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);
        channel.basicConsume(replyQueueName, true, new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                // 检查correlationId属性，如果它与请求中的值匹配，则返回对应用程序的响应
                if (properties.getCorrelationId().equals(corrId)) {
                    response.offer(new String(body, "UTF-8"));
                }
            }
        });
        return response.take();
    }

    public void close() throws IOException {
        connection.close();
    }

    public static void main(String[] args) throws IOException, TimeoutException, InterruptedException {
        RPCClient fibonacciRpc = new RPCClient();
        System.out.println(" [x] Requesting fib(30)");
        String response = fibonacciRpc.call("30");
        System.out.println(" [.] Got '" + response + "'");
        fibonacciRpc.close();
    }
}
