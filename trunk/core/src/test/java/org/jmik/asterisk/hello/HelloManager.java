package org.jmik.asterisk.hello;

import java.io.IOException;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;
import org.asteriskjava.manager.action.OriginateAction;
import org.asteriskjava.manager.response.ManagerResponse;
/**
 * Assumed a phone connected via SIP that is available at SIP/john 
 * and we want to initiate a call from that phone to extension 1300 
 * in the default context.
 * we have to obtain a ManagerConnection providing the hostname Asterisk
 * is running on and the username and password as configured
 * in manager.conf . 
 * Next we log in and send an OriginateAction and finally we disconnect.
 * @author michele
 *
 */
public class HelloManager {
	private ManagerConnection managerConnection;

	public HelloManager() throws IOException {
		ManagerConnectionFactory factory = new ManagerConnectionFactory(
				"localhost", "mark", "mysecret");

		this.managerConnection = factory.createManagerConnection();
	}

	public void run() throws IOException, AuthenticationFailedException,
			TimeoutException {
		OriginateAction originateAction;
		ManagerResponse originateResponse;

		originateAction = new OriginateAction();
		originateAction.setChannel("SIP/goldenboy");
		originateAction.setContext("default");
		originateAction.setExten("666");
		originateAction.setPriority(new Integer(1));
		originateAction.setTimeout(new Integer(30000));

		// connect to Asterisk and log in
		managerConnection.login();

		// send the originate action and wait for a maximum of 30 seconds for Asterisk
		// to send a reply
		originateResponse = managerConnection
				.sendAction(originateAction, 30000);

		// print out whether the originate succeeded or not
		System.out.println(originateResponse.getResponse());

		// and finally log off and disconnect
		managerConnection.logoff();
	}

	public static void main(String[] args) throws Exception {
		HelloManager helloManager;

		helloManager = new HelloManager();
		helloManager.run();
	}
}
