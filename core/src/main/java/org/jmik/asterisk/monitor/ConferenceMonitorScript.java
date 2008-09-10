package org.jmik.asterisk.monitor;

import org.apache.log4j.Logger;
import org.asteriskjava.fastagi.AgiChannel;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.BaseAgiScript;

/**
 * Default implementation of ConferenceMonitor Agi Script.
 * 
 * @author Michele La Porta
 *
 */
public class ConferenceMonitorScript extends BaseAgiScript{

	private static Logger logger = Logger.getLogger(ConferenceMonitorScript.class);

	private ConferenceCallMonitor conferenceMonitor;
	
	/**
	 * 
	 * @param conferenceMonitor
	 */
	public ConferenceMonitorScript(ConferenceCallMonitor conferenceMonitor) {
		this.conferenceMonitor = conferenceMonitor;
	}
	
	/* (non-Javadoc)
	 * @see org.asteriskjava.fastagi.AgiScript#service(org.asteriskjava.fastagi.AgiRequest, org.asteriskjava.fastagi.AgiChannel)
	 */
	public void service(AgiRequest agiRequest,AgiChannel agiChannel){
		String roomId = agiRequest.getParameter("roomId");
		logger.info("roomId " +roomId);
		if(conferenceMonitor.isMonitored(roomId)){
			logger.info(roomId + " not monitored");
			conferenceMonitor.monitorConference(roomId);
		}
	}
	
}
