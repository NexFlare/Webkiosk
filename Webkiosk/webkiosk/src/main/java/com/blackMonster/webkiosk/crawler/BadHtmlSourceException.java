package com.blackMonster.webkiosk.crawler;


public class BadHtmlSourceException extends Exception{
	public BadHtmlSourceException() {
	}
	
	 public BadHtmlSourceException(String message)
     {
        super(message);
     }
	 @Override
	public void printStackTrace() {
		// Log.e("BadHtmlSourceException", "Bad Html file");
		 super.printStackTrace();
	}
}
