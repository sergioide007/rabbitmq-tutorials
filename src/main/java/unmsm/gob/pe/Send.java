package unmsm.gob.pe;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;

public class Send {

    private final static String QUEUE_NAME = "events";

    public static void main(String[] argv) throws Exception {
    	Map<String, String> env = System.getenv();
        String rabbitmqUri = env.get("RABBITMQ_URL");
    	ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(rabbitmqUri);
        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();
		channel.queueDeclare(QUEUE_NAME, true, false, false, null);
		int prefetchCount = 1;
		channel.basicQos(prefetchCount); 
        Consumer consumer = new DefaultConsumer(channel) {
        	  @Override
        	  public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
        	    String message = new String(body, "UTF-8");

        	    System.out.println(" [x] Received '" + message + "'");
        	    try {
					doWork(message);
				} catch (InterruptedException e) {
					e.printStackTrace();
        	    } finally {
        	      System.out.println(" [x] Done");
        	      channel.basicAck(envelope.getDeliveryTag(), false);
        	    }
        	  }
        	};
       boolean autoAck = false; // acknowledgment is covered below
       channel.basicConsume(QUEUE_NAME, autoAck, consumer);       
       channel.close();
       connection.close();
       System.out.println(" Press [enter] to exit.");
    }
    
    private static void doWork(String task) throws InterruptedException {
        for (char ch: task.toCharArray()) {
            if (ch == '.') Thread.sleep(1000);
        }
    }
}
