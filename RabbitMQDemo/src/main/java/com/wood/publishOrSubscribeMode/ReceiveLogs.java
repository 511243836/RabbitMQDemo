package com.wood.publishOrSubscribeMode;

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
public class ReceiveLogs {
	public static void main(String[] args) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("192.168.253.102");
		factory.setUsername("admin");
		factory.setPassword("admin");
		
		Connection con = factory.newConnection();
		Channel c = con.createChannel();
		//创建logs的交换机
		c.exchangeDeclare("logs", "fanout");
		//创建队列，为自动生成的队列，该队列为非持久，独占，自动删除的类型
		String queueName = c.queueDeclare().getQueue();
		//把消息队列绑定到对应的交换机中，fanout类型的交换机，routingKey会被忽略
		c.queueBind(queueName, "logs","");
		System.out.println("等待接受消息:");
		//收到消息后用来处理消息的回掉对象
		DeliverCallback callback = new DeliverCallback() {
			@Override
			public void handle(String consumerTag, Delivery message) throws IOException {
				//获取message中的消息内容，该内容为一个byte数组，需要转为字符串
				String msg = new String(message.getBody(),"UTF-8");
				System.out.println("收到的消息为：" +msg);
			}
		};
		//消费者取消是的回掉对象
		CancelCallback cancel = new CancelCallback() {
			@Override
			public void handle(String consumerTag) throws IOException {
			}
		};
		//返回消息回执
		c.basicConsume(queueName, true,callback,cancel);
	}
}
