package org.jmik.asterisk.monitor;

import org.apache.log4j.Logger;
import org.asteriskjava.fastagi.AgiRequest;
import org.asteriskjava.fastagi.AgiScript;
import org.asteriskjava.fastagi.DefaultAgiServer;
import org.asteriskjava.fastagi.MappingStrategy;

/**
 * This class extends a DefaultAgiServer and use
 * MonitorScript as its mapping strategy
 * 
 * @author Michele La Porta
 *
 */
public class MonitorAgiServer extends DefaultAgiServer{

	private static Logger logger = Logger.getLogger(MonitorAgiServer.class);

	private MonitorScript monitorScript;
	
	/**
	 * 
	 * @param monitorScript
	 */
	public MonitorAgiServer(MonitorScript monitorScript) {
		if(monitorScript == null){
			logger.error("conferenceMonitorScript null");
			throw new IllegalStateException("conferenceMonitorScript cannot be null");
		}
		
		this.monitorScript = monitorScript;
		setMappingStrategy(new MonitorMappingStrategy());
		
	}
	
	/**
	 * Default implementation of MappingStrategy
	 */
	class MonitorMappingStrategy implements MappingStrategy{

		private Logger log = Logger.getLogger(MonitorMappingStrategy.class);

		public AgiScript determineScript(AgiRequest agiRequest) {
//			if(agiRequest.getScript().equals("conferenceMonitor")){
				log.info("determineScript " + monitorScript);
				return monitorScript;
//			}
//			return null;
		}
	}
	
}
