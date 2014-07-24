package com.email.listener;

import java.io.FileInputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;


import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.InternetAddress;



import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;

import javax.mail.*;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.InternetAddress;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;



public class ListenToEmails implements Runnable{


	private static  Logger logger =Logger.getLogger(ListenToEmails.class);
	
	private static String queue_new_email = "";
	 private static String queue_send_tweet = "";
	 private static String QUsername="";
	 private  static String QPassword="";
	 private static String _Email="";
	 private static String _Password="";
	 private static String _AlertEmailAddress="";
	 private static String _MyEmailAddress="";
	 private static String[] _TweetEmailAddress;
	 private static  Folder folder;
	 private static boolean filterEmail = true;
	 private static ConnectionFactory factory;
	 private static Connection connection;
	 private static Channel channel;
	 
	 static Double _FFLimit=0.0;
	
	public ListenToEmails(String filter)
	{
		if (filter.equals("-debug"))
		{
			filterEmail=false;
		
		}
		
		
	}
	private void AddHandler()
	{
		PropertyConfigurator.configure("/home/azureuser/EmailService/log4j.properties"); 
	//	PropertyConfigurator.configure("c:\\log4j.properties"); 
	}
	 public void run()
		{
			AddHandler();
			 logger.info("Starting up on thread "+Thread.currentThread().getId() ); 		
			
			 logger.info("Filtering emails =  "+filterEmail); 	
			
			
			GetConfig();
		
			
			if (initialiseQueue())
			{
			//	 new ListenForWebPings().start();		
				StartListenting();
			}
			
		}
		public ConnectionFactory getFactory()
		{
			return this.factory;
			
		}
		public Connection getConnection()
		{
			return this.connection;
			
		}
		public int getCount()
		{
			try {
				return folder.getMessageCount();
			} catch (MessagingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return 0;
		}
		private void GetConfig()
		{
			 try {
				 Properties props = new Properties();
				 logger.info("Processing config entries");
				props.load(new FileInputStream("/home/azureuser/EmailService/config.properties"));
			//	props.load(new FileInputStream("c:\\config.properties"));
				_Email = props.getProperty("email");
		    	_Password = props.getProperty("password");
		    	
		    	QUsername = props.getProperty("qusername");
		    	QPassword = props.getProperty("qpassword");
		    	queue_new_email = props.getProperty("queue_new_email");
		    	queue_send_tweet = props.getProperty("queue_send_tweet");
		   	_AlertEmailAddress = props.getProperty("alertemail");
		   // 	_TweetEmailAddress = props.getProperty("tweetemail");
		    	
		    	_TweetEmailAddress = props.getProperty("tweetemail").split(",");
		    	_MyEmailAddress = props.getProperty("myemail");
				_FFLimit = Double.valueOf(props.getProperty("fflimit"));
				 logger.info("Processing config entries complete");
			//	 logger.info(,"Using Config : Email : {0} , Password : {1}, QUsername : {2}, QPassword : {3}, AlertEmail : {4}, FFLimit {5}",new Object[]{_Email,_Password,QUsername,QPassword,_AlertEmailAddress,_FFLimit});
				 
			} catch (Exception e1) {
				
				 logger.warn(e1.toString());
				
			}
			
			
		}
		
		private void StartListenting()
		{
			
			 Properties props = new Properties();
			 
			 
			
			 final boolean FilterEmails=true;
			
			   props.setProperty("mail.store.protocol", "imaps");
		
			   
			   
			   
			   
			   try {
		            Session session = Session.getInstance(props, null);
		            Store store = session.getStore();
		       //     store.connect("imap-mail.outlook.com", _Email, _Password);
		        store.connect("imap.gmail.com", _Email, _Password);
		            //  final Folder folder =    store.getDefaultFolder();
		        
		            folder = store.getFolder("INBOX");
		          //  System.out.println("No of Unread Messages : " + folder.getUnreadMessageCount());
			         //   final IMAPFolder folder = (IMAPFolder) imapStore.getFolder("Inbox");
			            folder.open(Folder.READ_ONLY);
			            
			           
			            
			            folder.addMessageCountListener(new MessageCountListener() {

			               
							@Override
							public void messagesAdded(MessageCountEvent arg0) {
							
								 logger.info("New EMail Received");
								 try {
									Message msg = folder.getMessage(folder.getMessageCount());
									
									String from = InternetAddress.toString(msg.getFrom());
									 logger.info("SENT DATE : "+msg.getSentDate());
									 logger.info("FROM : "+from);
									 logger.info("SUBJECT : "+msg.getSubject());
							           
									
							 for (String s : _TweetEmailAddress)
							 {
								 if (from.contains(s))
								 {
									   logger.info("Routing email for tweet with subject "+msg.getSubject());
								         RouteMessage(s+": "+ msg.getSubject(),MessageType.TWEET); 
								 }
							 }

							
							 if (from.contains(_MyEmailAddress))
							 {
								 if (msg.getSubject().contains("BUY") || msg.getSubject().contains("SELL"))
								 {
								   logger.info("Routing email with subject {0}"+msg.getSubject());
							         RouteMessage(msg.getSubject(),MessageType.ORDER);
								 }
								 
							 }
							 
							 
							 

						     if (from.contains(_AlertEmailAddress) || !filterEmail)
								{					            

							     
							         logger.info("Routing email with subject "+msg.getSubject());
							         RouteMessage(msg.getSubject(),MessageType.ORDER);

							

							     }
						     else
							 {

							      logger.info("Email from : "+msg.getFrom()+" and not routed for execution");
							 }
						
								 
								 
								 }
							

							 							
								catch (MessagingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								 
								 
							}

							@Override
							public void messagesRemoved(MessageCountEvent arg0) {
								
							}
			            });

			            folder.addMessageChangedListener(new MessageChangedListener() {

			                public void messageChanged(MessageChangedEvent e) {
			                
			                }
			            });
			            Thread t1 = new Thread(
                		        new KeepAliveRunnable(folder), "IdleConnectionKeepAlive"
                		    );
	            	 t1.start();
			         			            
			           try{ 
			            while (!Thread.interrupted()) {
	                    	
	                        ((IMAPFolder) folder).idle();
	                    }
	                } catch (Exception ex) {
	                	 logger.warn(ex.toString() );
	                	 if (t1.isAlive()) {
	                	        t1.interrupt();
	                	    }
	                }
			   
			   
		  } catch (Exception mex) {
	            mex.printStackTrace();
	        }
		}
	                	 
		private boolean initialiseQueue()
		{
			try
			{
			logger.info("Connecting to queue "+queue_new_email);
			factory = new ConnectionFactory();
		    factory.setUsername(QUsername); 
			factory.setPassword(QPassword); 
			factory.setVirtualHost("/"); 
			factory.setHost("localhost");
			connection = factory.newConnection();
			channel  = connection.createChannel();
			channel.queueDeclare(queue_new_email, false, false, false, null);
			channel.queueDeclare(queue_send_tweet,false,false,false,null);
			logger.info("SUCCESS : Queue initialised and running");
			}
			catch (Exception e)
			{
				logger.fatal("Failed initialisation of queue : "+e.toString());
				logger.fatal("Is RabbitMq server running? to start the service type /sbin/service rabbitmq-server stop/start/etc.");
				return false;
			}
			return true;
		}
	private enum MessageType
	{
		ORDER,TWEET
	}
	
	private void RouteMessage(String message,MessageType type)
	{
		try{
			if (type.equals(MessageType.ORDER))
			{
		    channel.basicPublish("", queue_new_email, null, message.getBytes());
		      logger.info("Sent Email to queue "+queue_new_email+" : "+message);
			}
			else if (type.equals(MessageType.TWEET))
			{
				 channel.basicPublish("", queue_send_tweet, null, message.getBytes());
				 logger.info("Sent Tweet to queue "+queue_send_tweet+" : "+message);
			}
			
		}
		catch(Exception e)
		{
			logger.fatal("Unable to route message : "+e.toString());
		}
	}
	private static class KeepAliveRunnable implements Runnable {

		 //   private static final long KEEP_ALIVE_FREQ = 300000; // 5 minutes
			private static final long KEEP_ALIVE_FREQ = 5000;
		    private Folder folder;
		   

		    public KeepAliveRunnable(Folder folder2) {
		        this.folder = folder2;
		      
		    }

		    @Override
		    public void run() {
		    	 logger.info("Running Keep Alive on thread "+Thread.currentThread().getId() );
		        while (!Thread.interrupted()) {
		            try {
		                Thread.sleep(KEEP_ALIVE_FREQ);

		           //     Perform a NOOP just to keep alive the connection
		               logger.info("Performing a NOOP to keep the connection alive");
		               ((IMAPFolder) folder).doCommand(new IMAPFolder.ProtocolCommand() {
		                   public Object doCommand(IMAPProtocol p)
		                           throws ProtocolException {
		                       p.simpleCommand("NOOP", null);
		                     
								int i;
								try {
									i = folder.getMessageCount();
									logger.info("Message Count : "+i);
								} catch (MessagingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
									return null;	
		                              
		        
		        }
		               });
		           
		           } catch (Exception e) {
		                // Shouldn't really happen...
		            	
		            	logger.warn("Unexpected exception while keeping alive the IDLE connection "+e);
		            	
		   
		            	
		  				}
		           
		         }
		       }
		    }
		}


