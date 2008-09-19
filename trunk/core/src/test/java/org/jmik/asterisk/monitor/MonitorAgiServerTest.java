package org.jmik.asterisk.monitor;

import java.io.IOException;

public class MonitorAgiServerTest {

	public static void main(String[] args) {
		
		MonitorScript monitorScript = new MonitorScript();
		
		MonitorAgiServer agiServer = new MonitorAgiServer(monitorScript);
		
		try {
			agiServer.startup();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
