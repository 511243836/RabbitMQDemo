package com.wood.routingMode;

import java.io.IOException;
import java.util.Random;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 生产者
 * 路由模式：
 * 		  a/--
 * 	路由---/
 * 		  \
 * 		b.c\--
 * @author UID
 * 
 */
public class Producer {
	public static void main(String[] args) throws Exception {
		//定义路由键级别
		String[] type = {"a","b","c"};
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
		
		while (true) {
			System.out.println("请输入要发送的消息内容：");
			String msg = new Scanner(System.in).nextLine();
			//用于退出循环
			if ("exit".equals(msg)) {
				break;
			}
			
			//设置随机产生的消息级别
			String msgLevel = type[new Random().nextInt(type.length)];
			//发送消息（交换机名称，路由键的级别，其他配置属性，需要发布的消息内容）
			c.basicPublish("test", msgLevel, null, msg.getBytes());
			System.out.println("消息已发送" + msgLevel + "-" + msg);
			
		}
		//关闭连接
		con.close();
	}
}
