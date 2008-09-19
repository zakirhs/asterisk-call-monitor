package org.jmik.asterisk;

import java.io.IOException;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;
import org.jmik.asterisk.model.constants.Constants;

public class AsteriskManagerConnectionTest {

	public static void main(String[] args) {
		ManagerConnectionFactory managerConnectionFactory = new ManagerConnectionFactory(Constants.asteriskIpAddress,Constants.asteriskPort,Constants.asteriskManagerUser,Constants.asteriskManagerPassword);
		ManagerConnection managerConnection = managerConnectionFactory.createManagerConnection();
		managerConnection.addEventListener(new ManagerEventListenerImpl());
		
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
	
	
}
