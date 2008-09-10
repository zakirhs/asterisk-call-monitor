package org.jmik.asterisk.monitor;

import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.AgiScript;
import org.asteriskjava.fastagi.DefaultAgiServer;
import org.asteriskjava.fastagi.MappingStrategy;

/**
 * This class extends a DefaultAgiServer and use
 * ConferenceMonitorScript as its mapping strategy
 * 
 * @author Michele La Porta
 *
 */
public class ConferenceMonitorAgiServer extends DefaultAgiServer{

	private ConferenceMonitorScript conferenceMonitorScript;
	
	/**
	 * 
	 * @param conferenceMonitorScript
	 */
	public ConferenceMonitorAgiServer(ConferenceMonitorScript conferenceMonitorScript) {
		if(conferenceMonitorScript == null)
			throw new IllegalStateException("conferenceMonitorScript cannot be null");
		
		this.conferenceMonitorScript = conferenceMonitorScript;
		setMappingStrategy(new ConferenceMonitorMappingStrategy());
		
	}
	
	/**
	 * Default implementation of MappingStrategy
	 */
	class ConferenceMonitorMappingStrategy implements MappingStrategy{

		public AgiScript determineScript(AgiRequest agiRequest) {
			if(agiRequest.getScript().equals("conferenceMonitor")){
				return conferenceMonitorScript;
			}
			return null;
		}
	}
	
}
