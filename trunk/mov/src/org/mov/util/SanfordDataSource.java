package org.mov.util;

/* 
import javax.net.*;
import javax.net.ssl.*;
*/

import java.io.*;
import java.net.*;

public class SanfordDataSource implements InternetDataSource {
    
    public static void main(String[] args) {
	SanfordDataSource source = new SanfordDataSource();

	source.update();

    }

    public final static String HOST = "www.sanford.com.au";

    public void updateStockQuotes() {
	
	TradingDate latestQuoteDate = 
	    Database.getInstance().getLatestQuoteDate();
	TradingDate today = new TradingDate();
	
	// Are we possibly missing any dates?
	//	if(today.after(latestQuoteDate)) 
	    //	    update(latestQuoteDate, today);
    }


    //    private void update(TradingDate latestQuoteDate, TradingDate today) {
    public void update() {

	try {

	    System.out.println("url");

	    URL url = new URL("https", HOST, "/sanford/Login.asp");

	    System.out.println("connection");

	    URLConnection connection = url.openConnection();

	    System.out.println("content");

	    Object content = connection.getContent();

	    System.out.println("Content: ");
	    System.out.println(content);
	    System.out.println("done");

	}
	catch(java.io.IOException io) {
	    System.out.println("io exception" + io);
	}

    }

    /*
    private void update(TradingDate latestQuoteDate, TradingDate today) {
	
	try {
	    Socket socket = login();


	    logout(socket);
	} catch(java.io.IOException io) {

	}
    }

    private Socket login() throws java.io.IOException {
	// 1. Connect to the host
	SocketFactory factory = SSLSocketFactory.getDefault();
	Socket socket = 
	    factory.createSocket(HOST, PORT);
	InputStream input = socket.getInputStream();
	OutputStream output = socket.getOutputStream();
	
	
	return socket;
    }
    */
}


