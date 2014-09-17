package com.email.listener;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class StartListeners {
	static StartListeners INSTANCE;
	static ListenToEmails LTE_INSTANCE;
	static final Logger logger = Logger.getLogger(StartListeners.class);
	private SendTweet tw;
	
	public Logger getLogger()
	{
		return logger;
	}
	
	public static void main(String[] args)
	{
		
		INSTANCE = new StartListeners();
		
		INSTANCE.start(args);
		
		
	}
	
	private void start(String[] args)
	{
				
		PropertyConfigurator.configure("/home/azureuser/EmailService/log4j.properties"); 
	//	PropertyConfigurator.configure("c:\\log4j.properties"); 
		logger.info("Startup....Free memory : "+Runtime.getRuntime().freeMemory());
		boolean _HasErrored = false;	
	
		new SendTweet().start();
		
		
		
		
		int i =1;
		String arg="";
		if (args.length > 0)
		{
		arg = args[0];	
		}
		while (i <20)
		{
		logger.info("Start up attempt "+i);
		LTE_INSTANCE = new ListenToEmails(arg); 
		Thread thread = new Thread(LTE_INSTANCE);
		thread.start(); 
		logger.info("Started new thread");
		
		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			logger.warn(e.toString());
		}
		
			logger.info("thread died");
			
			if (!_HasErrored)
			{
				tw = new SendTweet();
				tw.SendNewTweet("Errors encountered with email listener. Cannot verify reliability");
				
				_HasErrored=true;
				
			}
			
			
			
		i++;
		}
		
		tw.SendNewTweet("Fatal error with email listener. Shutting down.");
		
	}
	
	
	
	
}
