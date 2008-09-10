package org.jmik.asterisk.monitor;

import org.apache.log4j.Logger;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.AgiScript;

/**
 * Default implementation of TwoPartiesMonitorScript Agi Script.
 * 
 * @author La Porta
 *
 */
public class TwoPartiesMonitorScript implements AgiScript {

	private static Logger logger = Logger.getLogger(TwoPartiesMonitorScript.class);

	private TwoPartiesMonitor twoPartiesMonitor;
	
	public TwoPartiesMonitorScript(TwoPartiesMonitor twoPartiesMonitor) {
		this.twoPartiesMonitor = twoPartiesMonitor;
	}
	
	public void service(AgiRequest agiRequest,AgiChannel agiChannel){
		String callId = agiRequest.getParameter("callId");
		logger.info("callId " +callId);
		
		if(twoPartiesMonitor.isMonitored(callId)){
			logger.info(callId + " not monitored");
			twoPartiesMonitor.monitorTwoParties(callId);
		}
	}
}
