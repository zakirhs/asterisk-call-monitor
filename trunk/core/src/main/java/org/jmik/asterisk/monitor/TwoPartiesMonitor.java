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
import org.jmik.asterisk.model.impl.TwoPartiesCall;

/**
 * Two Parties Monitor.
 * @author Michele La Porta
 *
 */
public class TwoPartiesMonitor implements CallListener,ProviderListener {

	private static Logger logger = Logger.getLogger(TwoPartiesMonitor.class);
	
	private List<Call> twoPartiesCalls;

	private HashMap<String, NoteTaker> noteTakers;
	
	public TwoPartiesMonitor(final String confMonitorIp,final  int confMonitorPort,final  String asteriskIp,final  int asteriskPort) {
		noteTakers = new HashMap<String, NoteTaker>();
		twoPartiesCalls = new ArrayList<Call>();
		
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
		if(call instanceof TwoPartiesCall){
			twoPartiesCalls.add(call);
			call.addListener(this);
			monitorTwoParties(call.getId());
			logger.info("callAttached " + twoPartiesCalls);
		}
	}

	public void callDetached(Call call) {
		if(call instanceof TwoPartiesCall){

			twoPartiesCalls.remove(call);
			call.removeListener(this);

			if(isMonitored(call.getId())){
				synchronized (noteTakers) {
					noteTakers.remove(call.getId());
					logger.info("callDetached " + noteTakers);
				}
			}

			logger.info("callDetached " + twoPartiesCalls);
		}
	}

	public boolean isMonitored(String callId){
		boolean res = noteTakers.containsKey(callId);
		logger.info("callId " + callId + " isMonitored " + res);
		return res;
		
	}

	public void monitorTwoParties(String callId){
		if(callId == null){
			throw new IllegalStateException("callId cannot be null");
		}
		synchronized (noteTakers) {
			NoteTaker noteTaker = new NoteTaker(callId);
			noteTaker.joinTwoParties();
			noteTakers.put(callId,noteTaker);
		}
	}

	public void unmonitorSingleParty(String callId){
		if(callId == null)
			throw new IllegalArgumentException("callId cannot be null");
		
		synchronized (noteTakers) {
			NoteTaker noteTaker = noteTakers.get(callId);
			if(noteTaker != null){
				noteTaker.leaveTwoParties();
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

	public List<Call> getTwoPartiesCalls() {
		return twoPartiesCalls;
	}
	class NoteTaker{
		
		String callId;
		
		public NoteTaker(String callId){
			this.callId = callId;
		}

		public void joinTwoParties() {
			logger.info("joinTwoParties callId " + callId);
			// send the INVITE
		}

		public void leaveTwoParties() {
			logger.info("leaveTwoParties callId " + callId);
			// send the BYE
		}

	}


}
