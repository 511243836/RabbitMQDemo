package com.wood.simpleMode;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
/**
 * 生产者
 * @author wood
 * 
 */
public class RabbitMQDemo1 {
	
	public static void main(String[] args) throws Exception {
		//	创建连接工厂，并设置连接信息
		ConnectionFactory f = new ConnectionFactory();
		f.setHost("192.168.253.102");
		//	默认可以不设置端口号
		f.setPort(5672);
		f.setUsername("admin");
		f.setPassword("admin");
		/*
		 * 	与rabbitmq服务器建立连接,
		 *	rabbitmq服务器端使用的是nio,会复用tcp连接,
		 * 	并开辟多个信道与客户端通信
		 * 	以减轻服务器端建立连接的开销
		 */
		Connection newConnection = f.newConnection();
		//	开辟信道与客户端通信
		Channel createChannel = newConnection.createChannel();
		
		//	定义一个队列，在服务器端新建这个队列，如果存在则使用已存在的
		createChannel.queueDeclare(
				"Hello World", 		//queue：队列名称
				false,				//durable：是否设置持久化，true表示RabbitMQ重启后队列仍存在
				false, 				//exclusive：排他,true表示限制仅当前连接可用
				false, 				//autoDelete：当最后一个消费者断开后,是否删除队列
				null);				//arguments：其他参数
		createChannel.basicPublish(
				"", 						//exchange: 交换机名称,空串表示默认交换机"(AMQP default)",不能用 null 
				"Hello World", 				//routingKey: 对于默认交换机,路由键就是目标队列名称
				null, 						//props: 其他参数,例如头信息
				"Hello World!!!".getBytes());	//body: 消息内容类型为byte[]数组
		System.out.println("消息已发送");
		//	断开连接
		createChannel.close();
	}
	
	
}
