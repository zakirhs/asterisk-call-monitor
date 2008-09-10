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

public class ConferenceCallMonitor implements CallListener,ProviderListener /*,SipListener*/{

	private static Logger logger = Logger.getLogger(ConferenceCallMonitor.class);

	private HashMap<String, NoteTaker> noteTakers;
	private List<Call> conferenceCalls;
	
	public ConferenceCallMonitor(final String confMonitorIp,final  int confMonitorPort,final  String asteriskIp,final  int asteriskPort) {
//		logger.info("ConferenceMonitor confMonitorIp="+confMonitorIp+" confMonitorPort="+confMonitorPort);
		noteTakers = new HashMap<String, NoteTaker>();
		conferenceCalls = new ArrayList<Call>();
	}
	
	public boolean isMonitored(String roomId){
		boolean res = noteTakers.containsKey(roomId);
		logger.info("roomId " + roomId + " isMonitored " + res);
		return res;		
//		synchronized (noteTakers) {
//			NoteTaker noteTaker = noteTakers.get(roomId);
//			if(noteTaker != null){
//				logger.info("roomId " + roomId + " monitored");
//				return true;
//			}else { 
//				logger.info("roomId " + roomId + " not monitored");
//				return false;
//			}
//		}
	}

	public void monitorConference(String roomId){
		if(roomId == null){
			throw new IllegalStateException("roomId cannot be null");
		}
		synchronized (noteTakers) {
			NoteTaker noteTaker = new NoteTaker(roomId);
			noteTaker.joinConference();
			noteTakers.put(roomId,noteTaker);
		}
	}

	public void unmonitorConference(String roomId){
		if(roomId == null)
			throw new IllegalArgumentException("roomId cannot be null");
		
		synchronized (noteTakers) {
			NoteTaker noteTaker = noteTakers.get(roomId);
			if(noteTaker != null){
				noteTaker.leaveConference();
				noteTakers.remove(roomId);
			}
		}
	}

	public void callAttached(Call call) {
		logger.info("callAttached call " + call + " removing this listener");
		call.addListener(this);

		if(call instanceof ConferenceCall){
			conferenceCalls.add(call);
			call.addListener(this);
			monitorConference(call.getId());
			logger.info("callAttached " + conferenceCalls);
		}
	}

	public void callDetached(Call call) {
		
		if(call instanceof ConferenceCall){

			conferenceCalls.remove(call);
			call.removeListener(this);

			if(isMonitored(call.getId())){
				synchronized (noteTakers) {
					noteTakers.remove(call.getId());
					logger.info("callDetached " + noteTakers);
				}
			}

			logger.info("callDetached " + conferenceCalls);
		}
	}

	public void channelAdded(ConferenceCall conferenceCall, Channel channel) {
		logger.info("channelAdded state " + conferenceCall + " channel " +channel);
		monitorConference(channel.getDescriptor().getId());
	}

	public void stateChanged(int newState, Call call) {
		if(call instanceof ConferenceCall){
			logger.info("stateChanged " + call + " newState " + newState);
			if(newState == Call.INVALID_STATE) {
				call.removeListener(this);
				unmonitorConference(call.getId());
			}
		}
	}

	public void channelRemoved(ConferenceCall conferenceCall, Channel channel) {
		logger.info("channelRemoved conferenceCall " + conferenceCall + " channel " +channel);
		// assuming the only one left is the notetaker
		if(conferenceCall.getChannels().size() == 1){
			unmonitorConference(conferenceCall.getId());
		}
	}

	public List<Call> getConferenceCalls() {
		return this.conferenceCalls;
	}

	class NoteTaker {

		String roomId;
		String callId;

		public NoteTaker(String roomId) {
			this.roomId = roomId;
		}

		public void joinConference() {
			logger.info("joinConference roomId " + roomId);
			// send the INVITE
		}

		public void leaveConference() {
			logger.info("leaveConference roomId " + roomId);
			// send the BYE
		}

	}
	
}
