package org.jmik.asterisk.gui;

import java.io.IOException;

import org.jmik.asterisk.monitor.ConferenceCallMonitor;
import org.jmik.asterisk.monitor.ConferenceMonitorAgiServer;

public class ConferenceMonitorAgiServerTest {

	public static void main(String[] args) {
		ConferenceCallMonitor conferenceMonitor = new ConferenceCallMonitor(
				"localhost", 4573, "localhost", 5038);
		org.jmik.asterisk.monitor.ConferenceMonitorScript monitorScript = new org.jmik.asterisk.monitor.ConferenceMonitorScript(
				conferenceMonitor);
//		conferenceMonitor.monitorConference("400");
		ConferenceMonitorAgiServer agiServer = new ConferenceMonitorAgiServer(
				monitorScript);
		try {
			agiServer.startup();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
