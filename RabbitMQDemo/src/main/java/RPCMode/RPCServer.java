package RPCMode;


import java.io.IOException;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.CancelCallback;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.Delivery;

public class RPCServer {
	//定义常量：消息队列名称
    private static final String RPC_QUEUE_NAME = "rpc_queue";
    
    //斐波那契方法
    private static int fib(int n) {
        if (n == 1) return 1;
        if (n == 2) return 1;
        return fib(n - 1) + fib(n - 2);
    }

    public static void main(String[] argv) throws Exception {
    	//创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        //设置IP，端口号默认的、用户名和密码
        factory.setHost("192.168.253.102");
        factory.setUsername("admin");
        factory.setPassword("admin");
        
        try {
        	//创建连接
			Connection con = factory.newConnection();
			//新建信道
			Channel c = con.createChannel();
			//设置消息队列（队列名，是否持久化，是否排他，是否自动删除，其他参数属性）
			c.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
			//清楚对应消息队列中的内容
			c.queuePurge(RPC_QUEUE_NAME);
			//一次只接收一条消息
			c.basicQos(1);
			
			System.out.println("等待接受Client消息:");
			
			//收到请求消息后的回调对象。通过回调对象执行对应的方法，获取client想要的数据
			DeliverCallback callback = new DeliverCallback() {
				@Override
				public void handle(String consumerTag, Delivery message) throws IOException {
					//通过AMQP.BasicProperties中builder方法建造一个与请求id一致的响应id
					//message.getProperties().getCorrelationId()获取请求id
					BasicProperties props = new BasicProperties().
							builder().
							correlationId(message.getProperties().getCorrelationId()).
							build();
					//设置一个response用于打印
					String response = "";
					try {
						//获取请求内容
						String msg = new String(message.getBody(),"UTF-8");
						int n = Integer.parseInt(msg);
						response += fib(n);
						System.out.println("第"+n+"个斐波那契数为：" + response);
					} catch (Exception e) {
						e.printStackTrace();
					}finally {
						//发送响应消息（默认交换机，客户端指定的消息队列名称，关联的id，返回的数据）
						c.basicPublish("", 
								message.getProperties().
								getReplyTo(),
								props, 
								response.getBytes());
						//发送确认消息
						c.basicAck(message.getEnvelope().getDeliveryTag(), false);
					}
				}
			};
			//回调接口，用于通知使用者取消操作。
			CancelCallback cencel = new CancelCallback() {
				
				@Override
				public void handle(String consumerTag) throws IOException {
					
				}
			};
			//消费者开始接收消息（消息队列名称，不自动接收，返回的数据等操作，通知使用者取消操作（但是内容为空））
			c.basicConsume(RPC_QUEUE_NAME, false,callback,cencel);
		} catch (Exception e) {
			e.printStackTrace();
		}

        
    }
}
