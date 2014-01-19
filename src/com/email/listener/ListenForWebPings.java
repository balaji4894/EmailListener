package com.email.listener;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;

import javax.mail.Folder;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class ListenForWebPings extends Thread{
	 private static String queue_new_email = "";
	 private static String QUsername="";
	 private  static String QPassword="";
	 private static String _Email="";
	 private static String _Password="";
	 private static String _AlertEmailAddress="";
	 private static  Folder folder;
	 public void run()
		{
		 try{
		 final String RPC_QUEUE_NAME = "rpc_queue";

		 ConnectionFactory factory = main.INSTANCE.getFactory();
		 
		 Connection connection =  main.INSTANCE.getConnection();
		 Channel channel = connection.createChannel();

		 channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);

		 channel.basicQos(1);

		 QueueingConsumer consumer = new QueueingConsumer(channel);
		 channel.basicConsume(RPC_QUEUE_NAME, false, consumer);

		 System.out.println(" [x] Awaiting RPC requests");

		 while (true) {
		     QueueingConsumer.Delivery delivery = consumer.nextDelivery();

		     BasicProperties props = delivery.getProperties();
		     BasicProperties replyProps = new BasicProperties
		                                      .Builder()
		                                      .correlationId(props.getCorrelationId())
		                                      .build();

		     String message = new String(delivery.getBody());
		     String response= "";
		     System.out.println("received request");
		    try
		    {
		     int count = main.INSTANCE.getCount();
		    
		     if (count > 0)
		     {
		    	 response = "CONNECTED";
		     }
		     else
		     {
		    	 response = "NOTCONNECTED";
		     }
		    }
		    catch (Exception e)
		    {
		    	response="UNKNOWN";
		    }
		     System.out.println(response);
		     channel.basicPublish( "", props.getReplyTo(), replyProps, response.getBytes());

		     channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
		     
		     
		 }
		 
		 
		 }
		 catch (Exception e)
		 {
			 System.out.println(e.toString());
			 
		 }
		 
		}
	 private void GetConfig()
		{
			 try {
				 Properties props = new Properties();
				
				props.load(new FileInputStream("c:\\config.properties"));
			
		    	QUsername = props.getProperty("qusername");
		    	QPassword = props.getProperty("qpassword");
		    	
			} catch (Exception e1) {
				
			
				
			}
			
			
		}
	
}
