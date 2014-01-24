package com.email.listener;



public class StartListeners {
	static ListenToEmails INSTANCE;
	
	public static void main(String[] args)
	{
		int i =1;
		String arg="";
		if (args.length > 0)
		{
		arg = args[0];	
		}
		while (i <20)
		{
		System.out.println("************Start up attempt "+i);
		INSTANCE = new ListenToEmails(arg); 
		Thread thread = new Thread(INSTANCE);
		thread.start(); 
		System.out.println("Started new thread");
		
		try {
			thread.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
			System.out.println("thread died");
		i++;
		}
		
		
	}
	
	
	
	
}
