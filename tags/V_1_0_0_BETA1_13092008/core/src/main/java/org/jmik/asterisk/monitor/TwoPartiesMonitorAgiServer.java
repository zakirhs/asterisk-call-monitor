package org.jmik.asterisk.monitor;

import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.AgiScript;
import org.asteriskjava.fastagi.DefaultAgiServer;
import org.asteriskjava.fastagi.MappingStrategy;

public class TwoPartiesMonitorAgiServer extends DefaultAgiServer{

	private TwoPartiesMonitorScript twoPartiesMonitorScript;
	
	public TwoPartiesMonitorAgiServer(TwoPartiesMonitorScript twoPartiesMonitorScript) {
		if(twoPartiesMonitorScript == null)
			throw new IllegalStateException("twoPartiesMonitorScript cannot be null");
		
		this.twoPartiesMonitorScript = twoPartiesMonitorScript;
		setMappingStrategy(new TwoPartiesyMonitorMappingStrategy());
		
	}
	
	class TwoPartiesyMonitorMappingStrategy implements MappingStrategy{

		public AgiScript determineScript(AgiRequest agiRequest) {
			if(agiRequest.getScript().equals("twoPartiesMonitor")){
				return twoPartiesMonitorScript;
			}
			return null;
		}
	}
	
}
