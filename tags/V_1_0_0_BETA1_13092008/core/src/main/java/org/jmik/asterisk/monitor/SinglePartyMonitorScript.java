package org.jmik.asterisk.monitor;

import org.apache.log4j.Logger;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.AgiScript;

/**
 * Default implementation of SinglePartyMonitor Agi Script.
 * @author Michele La Porta
 *
 */
public class SinglePartyMonitorScript implements AgiScript{

	private static Logger logger = Logger.getLogger(SinglePartyMonitorScript.class);

	private SinglePartyMonitor singlePartyMonitor;
	
	public SinglePartyMonitorScript(SinglePartyMonitor singlePartyMonitor) {
		this.singlePartyMonitor = singlePartyMonitor;
	}
	
	public void service(AgiRequest agiRequest,AgiChannel agiChannel){
		String callId = agiRequest.getParameter("callId");
		logger.info("callId " +callId);
		
		if(singlePartyMonitor.isMonitored(callId)){
			logger.info(callId + " not monitored");
			singlePartyMonitor.monitorSingleParty(callId);
		}
	}
}
