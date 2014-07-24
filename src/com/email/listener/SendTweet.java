package com.email.listener;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;


import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;

public class SendTweet extends Thread{
	//private static final Logger log = Logger.getLogger( SendTweet.class.getName() );
	private static  Logger logger =logger = Logger.getLogger(SendTweet.class);
	private static String queue_send_tweet = "";

	 private static String QUsername="";
	 private  static String QPassword="";
	 ConnectionFactory factory;
	 Connection connection;
	 private Channel channel;
	public void run()
	{
		try{
		PropertyConfigurator.configure("/home/azureuser/EmailService/log4j.properties"); 
	//	PropertyConfigurator.configure("c:\\log4j.properties"); 
	
		logger.info("From Tweet");
			Properties props = new Properties();
			logger.info("Processing config entries for tweeter");
			props.load(new FileInputStream("/home/azureuser/EmailService/config.properties"));
		//	props.load(new FileInputStream("c:\\config.properties"));
			QUsername = props.getProperty("qusername");
	    	QPassword = props.getProperty("qpassword");
	    	queue_send_tweet = props.getProperty("queue_send_tweet");
	   
	  	logger.info("Processing config entries complete for tweeter class");
	    	
	    	
	    	factory = new ConnectionFactory();
		    factory.setHost("localhost");
		    factory.setUsername(QUsername); 
			factory.setPassword(QPassword); 
			factory.setVirtualHost("/");   
		    
			 connection = factory.newConnection();
			 
			
		        Channel channel = connection.createChannel();

		        channel.queueDeclare(queue_send_tweet, false, false, false, null);
		
		        QueueingConsumer consumer = new QueueingConsumer(channel);
		        channel.basicConsume(queue_send_tweet, true, consumer);
		        logger.info("Twitter class waiting for new tweets on Queue : " + queue_send_tweet);
		        while (true) {
		          QueueingConsumer.Delivery delivery = consumer.nextDelivery();
		          String message = new String(delivery.getBody());
		          logger.info("Received new message to tweet : "+message);
		          SendNewTweet(message);
		        }
		
		}
		catch (Exception e)
		{
			logger.fatal(e.toString());
		}
		
		
	}
	
	
	
	
	
	

public void SendNewTweet(String message)
	
 {
		try{
		   TwitterFactory factory = new TwitterFactory();
		    AccessToken accessToken = loadAccessToken();
		    Twitter twitter = factory.getInstance();
		    twitter.setOAuthConsumer("uElgz3OkbjbZZ2edhxlw", "PQVMxp8W2oB0qinvQurRfDPfioVjG8vYg1hCL0tYnI");
		    twitter.setOAuthAccessToken(accessToken);
		  
		    
		    Status status = twitter.updateStatus(message);
		    logger.info("Successfully updated the status to [" + status.getText() + "].");
		    
		  }
		catch (Exception e)
		{
			logger.warn(e.toString());
		}
 }
		  private static AccessToken loadAccessToken(){
		    String token = "2300082091-SXJzIyt7ZYMSI86kqAFopxKoOy27fr1MnlLFmo5";
		    String tokenSecret = "iKGYQzwDPDCazZN0iXQUwUbf23CCHhXqdEEEIFTGSvZ56";
		    return new AccessToken(token, tokenSecret);
		  }
		
	
		

	}


