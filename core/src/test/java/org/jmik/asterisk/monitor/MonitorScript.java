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
public class MonitorScript extends BaseAgiScript{

	private static Logger logger = Logger.getLogger(MonitorScript.class);

	/**
	 * 
	 * @param conferenceMonitor
	 */
	public MonitorScript() {
		logger.info(this);
	}
	
	/* (non-Javadoc)
	 * @see org.asteriskjava.fastagi.AgiScript#service(org.asteriskjava.fastagi.AgiRequest, org.asteriskjava.fastagi.AgiChannel)
	 */
	public void service(AgiRequest agiRequest,AgiChannel agiChannel){
		logger.info("RequestURL " + agiRequest.getRequestURL());
		
		String id = agiRequest.getParameter("id");
		logger.info("id " +id);
		
	}
	
}
