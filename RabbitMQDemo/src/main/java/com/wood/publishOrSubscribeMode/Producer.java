package com.wood.publishOrSubscribeMode;

import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
/**
 * 生产者
 * @author wood
 *
 */
public class Producer {
	public static void main(String[] args) throws Exception {
		//创建连接工程
		ConnectionFactory factory = new ConnectionFactory();
		//设置rabbitMQ服务器的IP地址以及访问的用户名和密码
		factory.setHost("192.168.253.102");
		factory.setUsername("admin");
		factory.setPassword("admin");
		//创建连接
		Connection con = factory.newConnection();
		//创建信道
		Channel c = con.createChannel();
		//定义名字为logs的交换机,交换机类型为fanout
		//这一步是必须的，因为禁止发布到不存在的交换机
		c.exchangeDeclare("logs", "fanout");
		
		//模拟前端发送的请求数据
		while (true) {
			System.out.println("请输入要发送的消息：");
			String message = new Scanner(System.in).nextLine();
			if ("exit".equals(message)) {
				break;
			}
			//第一个参数,向指定的交换机发送消息的交换机名称
			//第二个参数,不指定队列,由消费者向交换机绑定队列
			//如果还没有队列绑定到交换器，消息就会丢失
			//即使没有消费者接收，我们也可以安全地丢弃这些信息。
			c.basicPublish("logs", "", null, message.getBytes());
		}
		c.close();
	}
}
