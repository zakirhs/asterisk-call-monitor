package org.mik.asterisk.model.monitor;

import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.AgiScript;
import org.asteriskjava.fastagi.DefaultAgiServer;
import org.asteriskjava.fastagi.MappingStrategy;

public class ConfMonitorAgiServer extends DefaultAgiServer {
	private ConferenceMonitorScript confMonitorScript;
	
	public ConfMonitorAgiServer(ConferenceMonitorScript confMonitorScript) {
		if(confMonitorScript == null) { 
			throw new IllegalArgumentException("confMonitorScript can not be null");
		}
		this.confMonitorScript = confMonitorScript;
		setMappingStrategy(new ConfMonitorMappingStrategy()); 
	}
	
	class ConfMonitorMappingStrategy implements MappingStrategy {
		public AgiScript determineScript(AgiRequest request) {
			if("conferenceMonitor".equals(request.getScript())) {
				return confMonitorScript;
			}			
			return null;
		}		
	}	
}