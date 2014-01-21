package com.email.listener;



public class StartListeners {
	static ListenToEmails INSTANCE;
	
	public static void main(String[] args)
	{
		
		while (true)
		{
		
		INSTANCE = new ListenToEmails(); 
		Thread thread = new Thread(INSTANCE);
		thread.start(); 
		System.out.println("Started new thread");
		while(thread.isAlive())
		{
			
			
			
		}
		System.out.println("thread died");
		
		}
		
		
	}
	
	
	
	
}
