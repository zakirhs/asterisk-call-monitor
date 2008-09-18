package org.jmik.asterisk.gui;

import java.io.IOException;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.event.ManagerEvent;
import org.jmik.asterisk.model.constants.Constants;

public class EventDumper implements ManagerEventListener,Runnable{
	
//	private static Logger logger = Logger.getLogger(EventDumper.class);

	private ManagerConnectionFactory managerConnectionFactory;
	private ManagerConnection managerConnection;
	
	public EventDumper() {
		managerConnectionFactory = new ManagerConnectionFactory(Constants.asteriskIpAddress,Constants.asteriskPort,Constants.asteriskManagerUser,Constants.asteriskManagerPassword);
		managerConnection = managerConnectionFactory.createManagerConnection();
		managerConnection.addEventListener(this);
		try {
			managerConnection.login();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (AuthenticationFailedException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		
	}
	
	public static void main(String[] args) {
		try {
			EventDumper eventDumper = new EventDumper();
			Thread t = new Thread(eventDumper);
			t.run();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		
	}

	public void onManagerEvent(ManagerEvent managerEvent) {
		System.out.println(managerEvent);
	}

	public void run() {
		while (true) {
			try {
				Thread.sleep(100);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

}
