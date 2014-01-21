package com.email.listener;

import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

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



public class ListenToEmails implements Runnable{


	private static final Logger log = Logger.getLogger( ListenToEmails.class.getName() );
	 private static String queue_new_email = "";
	 private static String QUsername="";
	 private  static String QPassword="";
	 private static String _Email="";
	 private static String _Password="";
	 private static String _AlertEmailAddress="";
	 private static  Folder folder;
	 private static boolean filterEmail = true;
	 private static ConnectionFactory factory;
	 private static Connection connection;
	 private static Channel channel;
	 
	 static Double _FFLimit=0.0;
	
	 public void run()
		{
			
			 log.log(Level.INFO ,"Starting up on thread {0}",Thread.currentThread().getId() ); 		
			
		
			
			
			GetConfig();
		
			
			if (initialiseQueue())
			{
				 new ListenForWebPings().start();		
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
				 log.log(Level.INFO ,"Processing config entries");
				props.load(new FileInputStream("c:\\config.properties"));
				_Email = props.getProperty("email");
		    	_Password = props.getProperty("password");
		    	
		    	QUsername = props.getProperty("qusername");
		    	QPassword = props.getProperty("qpassword");
		    	queue_new_email = props.getProperty("queue_new_email");
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
							
								 log.log(Level.INFO ,"New EMail Received");
								 try {
									Message msg = folder.getMessage(folder.getMessageCount());
									
									String from = InternetAddress.toString(msg.getFrom());
									 log.log(Level.INFO ,"SENT DATE : {0}",msg.getSentDate());
									 log.log(Level.INFO ,"FROM : {0}",from);
									 log.log(Level.INFO ,"SUBJECT : {0}",msg.getSubject());
							           
									
							        

										            

						     if (from.contains(_AlertEmailAddress) || !filterEmail)
								{					            

							     
							         log.log(Level.INFO ,"Routing email with subject {0}",msg.getSubject());
							         RouteMessage(msg.getSubject());

							

							      }
							    else
							       {

							      log.log(Level.INFO ,"Email not from : {0} and not routed",_AlertEmailAddress);
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
	                	 log.log(Level.INFO ,ex.toString() );
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
			log.log(Level.INFO, "Connecting to queue {0}",queue_new_email);
			factory = new ConnectionFactory();
		    factory.setUsername(QUsername); 
			factory.setPassword(QPassword); 
			factory.setVirtualHost("/"); 
			factory.setHost("localhost");
			connection = factory.newConnection();
			channel  = connection.createChannel();
			channel.queueDeclare(queue_new_email, false, false, false, null);
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
		 
		    channel.basicPublish("", queue_new_email, null, message.getBytes());
		      log.log(Level.INFO,"Sent Email to queue {0} : {1}",new Object[]{queue_new_email,message});
		}
		catch(Exception e)
		{
			log.log(Level.SEVERE, "Unable to route message : ",e.toString());
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
		    	 log.log(Level.INFO ,"Running Keep Alive on thread {0}",Thread.currentThread().getId() );
		        while (!Thread.interrupted()) {
		            try {
		                Thread.sleep(KEEP_ALIVE_FREQ);

		           //     Perform a NOOP just to keep alive the connection
		               log.log(Level.INFO,"Performing a NOOP to keep the connection alive");
		              ((IMAPFolder) folder).doCommand(new IMAPFolder.ProtocolCommand() {
		                   public Object doCommand(IMAPProtocol p)
		                           throws ProtocolException {
		                       p.simpleCommand("NOOP", null);
		                     
								int i;
								try {
									i = folder.getMessageCount();
									log.log(Level.INFO,"Message Count : {0}",i);
								} catch (MessagingException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								
									return null;	
		                              
		        
		        }
		               });
		           
		           } catch (Exception e) {
		                // Shouldn't really happen...
		            	
						
		            	
		            	log.log(Level.INFO,"Unexpected exception while keeping alive the IDLE connection {0}", e);
		            	
		           // /	  try {
		  		//			Runtime.getRuntime().exec("cmd /k start cmd /c java -jar C:\\EmailListener.jar ");
		  			///		log.log(Level.INFO,"Executed new Listener");
		  			//	} catch (Exception e1) {
		  					
		  					
		  				//		log.log(Level.INFO,e1.toString());
		  				
		            	
		  				}
		           
		         }
		       }
		    }
		}


