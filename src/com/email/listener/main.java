package com.email.listener;

import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.mail.*;
import javax.mail.event.MessageChangedEvent;
import javax.mail.event.MessageChangedListener;
import javax.mail.event.MessageCountEvent;
import javax.mail.event.MessageCountListener;
import javax.mail.internet.InternetAddress;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.QueueingConsumer;
import com.sun.mail.imap.IMAPFolder;


public class main {
	
	private static final Logger log = Logger.getLogger( main.class.getName() );
	 private final static String QUEUE_NEWEMAIL = "NEWEMAIL";
	 private static String QUsername="";
	 private  static String QPassword="";
	 private static String _Email="";
	 private static String _Password="";
	 private static String _AlertEmailAddress="";
	 
	 
	 
	 
	 static Double _FFLimit=0.0;
	 private void AttachLogHandler()
	 {
		 try
			{
			Date date = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Handler handler = new FileHandler("C:\\Users\\Ben\\IBLogs\\EmailListener"+sdf.format(date)+".log");
			log.addHandler(handler);
			}
			catch (Exception e)
			{
				System.out.println(e.toString());
			}
	 }
	public static void main(String[] args) {
		main m = new main();
		m.AttachLogHandler();
		m.GetConfig();
		if (m.initialiseQueue())
		{
			m.StartListenting();
		}
	}
	private void GetConfig()
	{
		 try {
			 Properties props = new Properties();
			 log.log(Level.INFO ,"Processing config entries");
			props.load(new FileInputStream("c:\\config.properties"));
			_Email = props.getProperty("email");
	    	_Password = props.getProperty("password");
	    	
	    	QUsername = props.getProperty("qusername");
	    	QPassword = props.getProperty("qpassword");
	    	_AlertEmailAddress = props.getProperty("alertemail");
			_FFLimit = Double.valueOf(props.getProperty("fflimit"));
			 log.log(Level.INFO ,"Processing config entries complete");
			 log.log(Level.INFO ,"Using Config : Email : {0} , Password : {1}, QUsername : {2}, QPassword : {3}, AlertEmail : {4}, FFLimit {5}",new Object[]{_Email,_Password,QUsername,QPassword,_AlertEmailAddress,_FFLimit});
			 
		} catch (Exception e1) {
			
			 log.log(Level.INFO ,e1.toString());
			
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
	            store.connect("imap.gmail.com", _Email, _Password);
	           
	            final Folder folder = store.getFolder("INBOX");
	            
		         //   final IMAPFolder folder = (IMAPFolder) imapStore.getFolder("Inbox");
		            folder.open(Folder.READ_WRITE);
		            
		           
		            
		            folder.addMessageCountListener(new MessageCountListener() {

		               
						@Override
						public void messagesAdded(MessageCountEvent arg0) {
						
							 log.log(Level.INFO ,"New EMail Received");
							 try {
								Message msg = folder.getMessage(folder.getMessageCount());
								
								String from = InternetAddress.toString(msg.getFrom());
								 log.log(Level.INFO ,"SENT DATE : {0}",msg.getSentDate());
								 log.log(Level.INFO ,"FROM : {0}",from);
								 log.log(Level.INFO ,"SUBJECT : {0}",msg.getSubject());
						           
								
						        
						       if (from.contains(_AlertEmailAddress))
								{					            
						     
						         log.log(Level.INFO ,"Routing email with subject {0}",msg.getSubject());
						         RouteMessage(msg.getSubject());
						        }
						       else
						       {
						    	   log.log(Level.INFO ,"Email from wrong sender, not routed");
						    	   
						       }
								
							} catch (MessagingException e) {
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
		            
		            Thread t = new Thread(new Runnable() {

			            public void run() {
			            	 log.log(Level.INFO ,"Listening for new emails" );
			                try {
			                    while (true) {
			                    	
			                        ((IMAPFolder) folder).idle();
			                    }
			                } catch (MessagingException ex) {
			                    //Handling exception goes here
			                }
			            }
			        });

	        t.start();
		            
		            
		            
		        } catch (Exception mex) {
		            mex.printStackTrace();
		        }
		} 
	
	private ConnectionFactory factory;
	private Connection connection;
	private Channel channel;
	
	private boolean initialiseQueue()
	{
		try
		{
		log.log(Level.INFO, "Connecting to queue {0}",QUEUE_NEWEMAIL);
		factory = new ConnectionFactory();
	    factory.setUsername(QUsername); 
		factory.setPassword(QPassword); 
		factory.setVirtualHost("/"); 
		factory.setHost("localhost");
		connection = factory.newConnection();
		channel  = connection.createChannel();
		channel.queueDeclare(QUEUE_NEWEMAIL, false, false, false, null);
		log.log(Level.INFO, "SUCCESS : Queue initialised and running");
		}
		catch (Exception e)
		{
			log.log(Level.SEVERE, "Failed initialisation of queue : {0}",e.toString());
			return false;
		}
		return true;
	}
	
private void RouteMessage(String message)
{
	try{
	 
	    channel.basicPublish("", QUEUE_NEWEMAIL, null, message.getBytes());
	      log.log(Level.INFO,"Sent Email to queue {0} : {1}",new Object[]{QUEUE_NEWEMAIL,message});
	}
	catch(Exception e)
	{
		log.log(Level.SEVERE, "Unable to route message : ",e.toString());
	}
}
}


