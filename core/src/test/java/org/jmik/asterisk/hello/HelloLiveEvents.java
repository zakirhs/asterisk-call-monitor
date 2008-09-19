package org.jmik.asterisk.hello;

import org.asteriskjava.live.AsteriskChannel;
import org.asteriskjava.live.AsteriskServer;
import org.asteriskjava.live.AsteriskServerListener;
import org.asteriskjava.live.DefaultAsteriskServer;
import org.asteriskjava.live.ManagerCommunicationException;
import org.asteriskjava.live.MeetMeUser;

public class HelloLiveEvents implements AsteriskServerListener {
	private AsteriskServer asteriskServer;

	public HelloLiveEvents() {
		asteriskServer = new DefaultAsteriskServer("localhost", "mark",
				"mysecret");
	}

	public void run() throws ManagerCommunicationException {
		asteriskServer.addAsteriskServerListener(this);
	}

	public void onNewAsteriskChannel(AsteriskChannel channel) {
		System.out.println(channel);
	}

	public void onNewMeetMeUser(MeetMeUser user) {
		System.out.println(user);
	}

	public static void main(String[] args) throws Exception {
		HelloLiveEvents helloLiveEvents = new HelloLiveEvents();
		helloLiveEvents.run();
	}
}