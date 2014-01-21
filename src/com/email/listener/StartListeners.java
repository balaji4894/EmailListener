package com.email.listener;



public class StartListeners {
	static ListenToEmails INSTANCE;
	
	public static void main(String[] args)
	{
		int i =1;
		
		while (i <100)
		{
		System.out.println("************Start up attempt "+i);
		INSTANCE = new ListenToEmails(); 
		Thread thread = new Thread(INSTANCE);
		thread.start(); 
		System.out.println("Started new thread");
		
			while(thread.isAlive())
			{
				
				
				
			}
		
			System.out.println("thread died");
		i++;
		}
		
		
	}
	
	
	
	
}
