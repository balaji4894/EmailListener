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
import com.sun.mail.iap.ProtocolException;
import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.protocol.IMAPProtocol;


public class main {
	
	private static final Logger log = Logger.getLogger( main.class.getName() );
	 private final static String QUEUE_NEWEMAIL = "NEWEMAIL";
	 private final static String QUEUE_WEBREQUEST = "EMAILWEBREQUEST";
	 private final static String QUEUE_WEBRESPONSE = "EMAILWEBRESPONSE";
	 private static String QUsername="";
	 private  static String QPassword="";
	 private static String _Email="";
	 private static String _Password="";
	 private static String _AlertEmailAddress="";
	 private static  Folder folder;

		private static ConnectionFactory factory;
		private static Connection connection;
		private static Channel channel;
	 
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
		m.initialiseQueue();
		Thread t1 = new Thread(new Runnable() {

	            public void run() {
	            	 log.log(Level.INFO ,"Listening for new web querys" );
	               
	            	 Thread t1 = new Thread(
               		        new SetupWebListener(folder,QUsername,QPassword,_Email,_Password,connection), "IdleConnectionKeepAlive"
               		    );
	            	 
	            	 
	            	 
	            }
		});
	            	 t1.start();
		
	           
		
		
		//if ()
	//	{
			m.StartListenting();
		//}
		
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
	private static class SetupWebListener implements Runnable
	{
		
		 private final static String QUEUE_WEBREQUEST = "EMAILWEBREQUEST";
		 private final static String QUEUE_WEBRESPONSE = "EMAILWEBRESPONSE";
		 private static String QUsername="";
		 private  static String QPassword="";
		 private static String _Email="";
		 private static String _Password="";
		 private Connection connection;
		 private  Folder folder;
		
		
		
		public SetupWebListener(Folder folder, String QUsername, String QPassword, String _Email, String _Pasword, Connection Connection) {
	        this.folder = folder;
	        this.QPassword = QPassword;
	        this._Email = _Email;
	        this._Password = _Password;
	        this.connection = Connection;
	               
	        
	    }
		
	
		 @Override
		    public void run() {
	
		try{
		
	//		Properties props = new Properties();
	//		log.log(Level.INFO ,"Processing config entries");
	//		props.load(new FileInputStream("c:\\config.properties"));
	//		QUsername = props.getProperty("qusername");
	 //   	QPassword = props.getProperty("qpassword");
	  //  	log.log(Level.INFO ,"Processing config entries complete");
	   // 	factory = new ConnectionFactory();
		//    factory.setHost("localhost");
		 //   factory.setUsername(QUsername); 
		//	factory.setPassword(QPassword); 
		//	factory.setVirtualHost("/");   
		    
		   // connection = factory.newConnection();
		    Channel channel_Recv = connection.createChannel();
		    Channel channel_Send = connection.createChannel();
		    channel_Recv.queueDeclare(QUEUE_WEBREQUEST, false, false, false, null);
		    channel_Send.queueDeclare(QUEUE_WEBRESPONSE, false, false, false, null);
		    
		   
		    
		    QueueingConsumer consumer = new QueueingConsumer(channel_Recv);
		    channel_Recv.basicConsume(QUEUE_WEBREQUEST, true, consumer);
		    while (true) {
			      log.log(Level.INFO,"EMAIL waiting for web querys on Queue : {0}",QUEUE_WEBREQUEST);
			      QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			      String message = new String(delivery.getBody());
			      log.log(Level.INFO,"Received new message on Topic {0} : {1}",new Object[]{QUEUE_WEBREQUEST,message});
			      String Response="";
			      Response = Integer.toString(folder.getMessageCount());
			      channel_Send.basicPublish("", QUEUE_WEBRESPONSE, null, Response.getBytes());
			      log.log(Level.INFO,"Sent WebReply message on Topic {0} : {1}",new Object[]{QUEUE_WEBRESPONSE,Response});
		    }
		}
		catch (Exception e)
		{
			 log.log(Level.INFO,e.toString());
		}
	
	}}
	
	private void StartListenting()
	{
		
		 Properties props = new Properties();
		 
		 
		
		 final boolean FilterEmails=true;
		
		   props.setProperty("mail.store.protocol", "imaps");
	
		   
		   
		   
		   
		   try {
	            Session session = Session.getInstance(props, null);
	            Store store = session.getStore();
	        //    store.connect("imap-mail.outlook.com", _Email, _Password);
	         store.connect("imap.gmail.com", _Email, _Password);
	            //  final Folder folder =    store.getDefaultFolder();
	        
	            folder = store.getFolder("INBOX");
	          //  System.out.println("No of Unread Messages : " + folder.getUnreadMessageCount());
		         //   final IMAPFolder folder = (IMAPFolder) imapStore.getFolder("Inbox");
		            folder.open(Folder.READ_ONLY);
		            
		           
		            
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
						      log.log(Level.INFO ,"Email not from : {0} and not routed",_AlertEmailAddress);
						    	   
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
			               
			            	 Thread t1 = new Thread(
		                		        new KeepAliveRunnable(folder), "IdleConnectionKeepAlive"
		                		    );
			            	 t1.start();
			            	 
			            	 try {
			                	
			                	

			                		   
			                	
			                	
			                    while (!Thread.interrupted()) {
			                    	
			                        ((IMAPFolder) folder).idle();
			                    }
			                } catch (Exception ex) {
			                	 log.log(Level.INFO ,ex.toString() );
			                	 if (t1.isAlive()) {
			                	        t1.interrupt();
			                	    }
			                	 
			                }
			            }
			        });

	        t.start();
		            
		            
		            
		        } catch (Exception mex) {
		            mex.printStackTrace();
		        }
		} 
	

	
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

private static class KeepAliveRunnable implements Runnable {

    private static final long KEEP_ALIVE_FREQ = 300000; // 5 minutes

    private Folder folder;

    public KeepAliveRunnable(Folder folder2) {
        this.folder = folder2;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            try {
                Thread.sleep(KEEP_ALIVE_FREQ);

                // Perform a NOOP just to keep alive the connection
                log.log(Level.INFO,"Performing a NOOP to keep alvie the connection");
                ((IMAPFolder) folder).doCommand(new IMAPFolder.ProtocolCommand() {
                    public Object doCommand(IMAPProtocol p)
                            throws ProtocolException {
                        p.simpleCommand("NOOP", null);
                        return null;
                    }
                });
            } catch (InterruptedException e) {
            	log.log(Level.WARNING,"Aborting thread");
            } catch (MessagingException e) {
                // Shouldn't really happen...
            	log.log(Level.INFO,"Unexpected exception while keeping alive the IDLE connection {0}", e);
            }
        }
    }
}
}

