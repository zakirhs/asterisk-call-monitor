package org.jmik.asterisk.monitor;

import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.AgiScript;
import org.asteriskjava.fastagi.DefaultAgiServer;
import org.asteriskjava.fastagi.MappingStrategy;

public class SinglePartyMonitorAgiServer extends DefaultAgiServer{

	private SinglePartyMonitorScript singlePartyMonitorScript;
	
	public SinglePartyMonitorAgiServer(SinglePartyMonitorScript singlePartyMonitorScript) {
		if(singlePartyMonitorScript == null)
			throw new IllegalStateException("conferenceMonitorScript cannot be null");
		
		this.singlePartyMonitorScript = singlePartyMonitorScript;
		setMappingStrategy(new SinglePartyMonitorMappingStrategy());
		
	}
	
	class SinglePartyMonitorMappingStrategy implements MappingStrategy{

		public AgiScript determineScript(AgiRequest agiRequest) {
			if(agiRequest.getScript().equals("singlePartyMonitor")){
				return singlePartyMonitorScript;
			}
			return null;
		}
	}
	
}
