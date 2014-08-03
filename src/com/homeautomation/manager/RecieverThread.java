package com.homeautomation.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class RecieverThread implements Runnable{
	boolean flag = true;
	ServerSocket ss;
	int port = 10001;
	BufferedReader fr;
	String message ="";
	@Override
	public void run() {
		while(flag){
			try {
				Socket socket = ss.accept();
				fr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				String line = "";
				while((line = fr.readLine()) != null) {
				    System.out.println(line);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} 
		}
	}
	public  RecieverThread(){
		flag = true;
		try {
			ss = new ServerSocket(port);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public void stop() {
		flag = false;
	}
	public static void main(String args[]){
		RecieverThread thread = new RecieverThread();
		new Thread(thread).start();
		
	}
}
