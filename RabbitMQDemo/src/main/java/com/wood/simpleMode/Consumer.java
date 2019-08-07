package com.wood.simpleMode;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;
/**
 * 消费者
 * @author wood
 * 
 */
public class Consumer {
	public static void main(String[] args) throws Exception {
		//	创建连接工厂
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("192.168.253.102");
		factory.setUsername("admin");
		factory.setPassword("admin");
		//	建立连接
		Connection connection = factory.newConnection();
		//	建立信道
		Channel channel = connection.createChannel();
		//	声明队列，没有则创建，有则直接使用
		channel.queueDeclare("Hello World", false, false, false, null);
		
		//	收到消息后用来处理消息的回调对象
		DeliverCallback callback = new DeliverCallback() {
			@Override
			public void handle(String consumerTag, Delivery message) throws IOException {
				//	通过message获取内容
				byte[] bodys = message.getBody();
				//	转为string类型字符串
				String body = new String(bodys);
				System.out.println("收到的消息为：" + body);
			}
		};
		//	费者取消时的回调对象
		CancelCallback cancel = new CancelCallback() {
			@Override
			public void handle(String consumerTag) throws IOException {
				
			}
		};
		channel.basicConsume("Hello World", true,callback,cancel);
	}
}
