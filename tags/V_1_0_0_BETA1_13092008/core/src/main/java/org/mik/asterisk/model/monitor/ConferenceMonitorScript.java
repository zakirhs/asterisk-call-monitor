package org.mik.asterisk.model.monitor;

import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiException;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;


public class ConferenceMonitorScript extends BaseAgiScript {	
	private ConferenceMonitor confMonitor;
	
	public ConferenceMonitorScript(ConferenceMonitor confMonitor) {
		if(confMonitor == null) throw new IllegalArgumentException("confMonitor can not be null");
		this.confMonitor = confMonitor;
	}

	public void service(AgiRequest request, AgiChannel channel)
		throws AgiException {
		String roomId = request.getParameter("roomId");
		System.out.println("service.roomId : " + roomId);
		if(!confMonitor.isMonitored(roomId)) {
			System.out.println(roomId + " is not monitored");
			confMonitor.monitorConference(roomId);
		}
	}
}
