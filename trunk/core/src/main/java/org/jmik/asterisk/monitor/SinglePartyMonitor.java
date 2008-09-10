package org.jmik.asterisk.monitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.jmik.asterisk.model.CallListener;
import org.jmik.asterisk.model.ProviderListener;
import org.jmik.asterisk.model.impl.Call;
import org.jmik.asterisk.model.impl.Channel;
import org.jmik.asterisk.model.impl.ConferenceCall;
import org.jmik.asterisk.model.impl.SinglePartyCall;

/**
 * Single Party Monitor.
 * @author Michele La Porta
 *
 */
public class SinglePartyMonitor implements CallListener,ProviderListener {

	private static Logger logger = Logger.getLogger(SinglePartyMonitor.class);
	
	private List<Call> singlePartyCalls;

	private HashMap<String, NoteTaker> noteTakers;
	
	public SinglePartyMonitor(final String confMonitorIp,final  int confMonitorPort,final  String asteriskIp,final  int asteriskPort) {
		noteTakers = new HashMap<String, NoteTaker>();
		singlePartyCalls = new ArrayList<Call>();
		
	}

	public void stateChanged(int newState, Call call) {
		if(call instanceof SinglePartyCall){
			logger.info("stateChanged " + call + " newState " + newState);
			if(newState == Call.INVALID_STATE) {
				call.removeListener(this);
				unmonitorSingleParty(call.getId());
			}
		}
	}

	public void callAttached(Call call) {
		if(call instanceof SinglePartyCall){
			singlePartyCalls.add(call);
			call.addListener(this);
			monitorSingleParty(call.getId());
			logger.info("callAttached " + singlePartyCalls);
		}
	}

	public void callDetached(Call call) {
		if(call instanceof SinglePartyCall){
			singlePartyCalls.remove(call);
			call.removeListener(this);
			logger.info("callDetached " + singlePartyCalls);
		}
	}

	public boolean isMonitored(String callId){
		return true;
	}

	public void monitorSingleParty(String callId){
		if(callId == null){
			throw new IllegalStateException("callId cannot be null");
		}
		synchronized (noteTakers) {
			NoteTaker noteTaker = new NoteTaker(callId);
			noteTaker.joinSingleParty();
			noteTakers.put(callId,noteTaker);
		}
	}

	public void unmonitorSingleParty(String callId){
		if(callId == null)
			throw new IllegalArgumentException("callId cannot be null");
		
		synchronized (noteTakers) {
			NoteTaker noteTaker = noteTakers.get(callId);
			if(noteTaker != null){
				noteTaker.leaveSingleParty();
				noteTakers.remove(callId);
			}
		}
	}

	public void channelAdded(ConferenceCall conferenceCall, Channel channel) {
//		logger.info("channelAdded conferenceCall " +conferenceCall+" channel"+channel);
	}

	public void channelRemoved(ConferenceCall conferenceCall, Channel channel) {
//		logger.info("channelRemoved conferenceCall " +conferenceCall+" channel"+channel);
	}

	public HashMap<String, NoteTaker> getNoteTakers() {
		return noteTakers;
	}

	public List<Call> getSinglePartyCalls() {
		return singlePartyCalls;
	}

	class NoteTaker{
		
		String callId;
		
		public NoteTaker(String callId){
			this.callId = callId;
		}

		public void joinSingleParty() {
			logger.info("joinSingleParty callId " + callId);
			// send the INVITE
		}

		public void leaveSingleParty() {
			logger.info("leaveSingleParty callId " + callId);
			// send the BYE
		}

	}


}
