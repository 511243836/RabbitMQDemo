package com.wood.workMode;

import java.io.IOException;

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
public class Consumer1 {
	public static void main(String[] args) throws Exception {
		//连接工厂
		ConnectionFactory f = new ConnectionFactory();
		f.setHost("192.168.253.102");
		f.setUsername("admin");
		f.setPassword("admin");
		
		//建立连接
		Connection c = f.newConnection();
		//建立信道
		Channel ch = c.createChannel();
		//声明队列,如果该队列已经创建过,则不会重复创建
		//true设置队列持久化
		ch.queueDeclare("task queue",true,false,false,null);
		System.out.println("等待接收数据");
		
		//一次只接受一条消息
		ch.basicQos(1);
		
		//收到消息后用来处理消息的回调对象
		DeliverCallback callback = new DeliverCallback() {
			@Override
			public void handle(String consumerTag, Delivery message) throws IOException {
//				通过message获取内容
				byte[] bodys = message.getBody();
				//	转为string类型字符串
				String body = new String(bodys);
				System.out.println("收到的消息为：" + body);
				for (int i = 0; i < body.length(); i++) {
					if (body.charAt(i) == '.') {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
				System.out.println("处理结束");
				//发送回执	消息标签，是否确认全部消息
				ch.basicAck(message.getEnvelope().getDeliveryTag(), false);
			}
		};
		//消费者取消时的回调对象
		CancelCallback cancel = new CancelCallback() {
			
			@Override
			public void handle(String consumerTag) throws IOException {
				
			}
		};
		
		ch.basicConsume("task queue", false,callback,cancel);
		
	}
}
