package RPCMode;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

import java.io.IOException;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

public class RPCClient implements AutoCloseable {

    private Connection connection;
    private Channel channel;
    private String requestQueueName = "rpc_queue";

    public RPCClient() throws Exception {
    	//创建连接工厂
    	ConnectionFactory factory = new ConnectionFactory();
    	//设置IP，端口号默认的、用户名和密码
        factory.setHost("192.168.253.102");
        factory.setUsername("admin");
        factory.setPassword("admin");

        //创建连接
        connection = factory.newConnection();
        //新建信道
        channel = connection.createChannel();
    }

    public static void main(String[] argv) {
        try {
        	//new RPCClient实例，初始化创建连接工厂等操作
			RPCClient client = new RPCClient();
			while (true) {
				System.out.print("求第几个斐波那契数:");
				String n = new Scanner(System.in).nextLine();
				//调用call（）方法
				String r = client.call(n);
				System.out.println("返回的结果为：" + r);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} 
    }

    public String call(String message) throws Exception {
    	//生成关联id
        final String corrId = UUID.randomUUID().toString();
        //自动生成消息队列名称，并且该消息队列非持久化，独占，自动删除
        String replyQueueName = channel.queueDeclare().getQueue();
        //设置请求和响应的关联id和传递响应的数据的消息队列， build()前可以通过Builder（）.可以设置多个参数 
        BasicProperties props = new BasicProperties.
        		Builder().
        		correlationId(corrId).
        		replyTo(replyQueueName).
        		build();
        //发送请求（默认交换机，消息队列名称，关联id和传递响应的数据的消息队列，请求第N个斐波那契数）
        channel.basicPublish("", requestQueueName, props, message.getBytes("UTF-8"));
        //新建阻塞集合，用来保存结果，取数据的时候，没有就会暂时等待
        final BlockingQueue<String> response = new ArrayBlockingQueue<>(1);
        //收到请求消息后的回调对象。通过回调对象执行对应的方法，获取server返回的数据
        DeliverCallback callback = new DeliverCallback() {
			
			@Override
			public void handle(String consumerTag, Delivery message) throws IOException {
				//判断server发送的关联id和自己的关联id是否相同
				if (message.getProperties().getCorrelationId().equals(corrId)) {
					//将相应内容放入到阻塞集合
	                response.offer(new String(message.getBody(), "UTF-8"));
	            }
			}
		};
		
		//回调接口，用于通知使用者取消操作。
		CancelCallback cencel = new CancelCallback() {
			@Override
			public void handle(String consumerTag) throws IOException {
			}
		};
		//开始从队列接收响应数据
		channel.basicConsume(replyQueueName, true,callback,cencel);
		//返回保存在集合中的响应数据
        String result = response.take();
        return result;
    }

    public void close() throws IOException {
        connection.close();
    }
}
