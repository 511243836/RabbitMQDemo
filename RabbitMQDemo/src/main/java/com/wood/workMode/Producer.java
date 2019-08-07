package com.wood.workMode;

import java.util.Scanner;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
/**
 * 消费者
 * @author wood
 * 
 */
public class Producer {
	public static void main(String[] args) throws Exception {
		ConnectionFactory f = new ConnectionFactory();
		f.setHost("192.168.253.102");
		f.setPort(5672);
		f.setUsername("admin");
		f.setPassword("admin");
		
		Connection c = f.newConnection();
		Channel ch = c.createChannel();
		//参数:queue,durable,exclusive,autoDelete,arguments
		//true 设置队列持久化
		ch.queueDeclare("task queue", true,false,false,null);

		while (true) {
		    //控制台输入的消息发送到rabbitmq
			System.out.print("输入消息: ");
			String msg = new Scanner(System.in).nextLine();
			//如果输入的是"exit"则结束生产者进程
			if ("exit".equals(msg)) {
				break;
			}
			//参数:exchage,routingKey,props,body
			//消息的持久化
			ch.basicPublish("", 
					"task queue", 
					MessageProperties.PERSISTENT_TEXT_PLAIN, 
					msg.getBytes("UTF-8"));
			System.out.println("消息已发送: "+msg);
		}

		c.close();
	}
}
