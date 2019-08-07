package com.wood.routingMode;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

/**
 * 消费者
 * @author UID
 *
 */
public class Consumer {
	public static void main(String[] args) throws Exception {
		//创建连接工厂及配置rabbitmq服务器的IP地址还有端口号+用户名+密码
		ConnectionFactory factory = new ConnectionFactory();
		//填写自己RabbitMQ服务器IP地址、端口号、用户名、密码
		factory.setHost("192.168.253.102");
		factory.setPort(5762);
		factory.setUsername("admin");
		factory.setPassword("admin");
		//新建连接
		Connection con = factory.newConnection();
		//创建信道
		Channel c = con.createChannel();
		//设置交换器的名称以及交换机的类型
		c.exchangeDeclare("test", BuiltinExchangeType.DIRECT);
		//自动生成队列名，并且为非持久、独占、自动删除的
		String queueName = c.queueDeclare().getQueue();
		System.out.println("接收的内容级别用空格隔开：");
		//获取想要获取的类型
		String[] bodyType = new Scanner(System.in).nextLine().split("\\s");
		//将队列绑定到test交换机，运行使用多个bindingKey
		for (String level : bodyType) {
			c.queueBind(queueName, "test", level);
		}
		System.out.println("等待接收数据：");
		
		//收到消息后用来处理消息的回调对象
		DeliverCallback callback = new DeliverCallback() {
			@Override
			public void handle(String consumerTag, Delivery message) throws IOException {
				String msg = new String(message.getBody(), "UTF-8");
				String routingKey = message.getEnvelope().getRoutingKey();
				System.out.println("收到: "+routingKey+" - "+msg);
			}
		};
				
		//消费者取消时的回调对象
		CancelCallback cancel = new CancelCallback() {
			@Override
				public void handle(String consumerTag) throws IOException {
			}
		};
				
		c.basicConsume(queueName, true, callback, cancel);
	}
}
