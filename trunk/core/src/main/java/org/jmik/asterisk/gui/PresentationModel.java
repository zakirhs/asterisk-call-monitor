package org.jmik.asterisk.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jmik.asterisk.model.impl.AsteriskProvider;
import org.jmik.asterisk.model.impl.Call;
import org.jmik.asterisk.model.impl.Channel;
import org.jmik.asterisk.model.impl.ConferenceCall;

/**
 * Model view controller for asterisk call monitor.
 * Use AsteriskProvider for monitor and drop.
 * Push notification to its listeners.
 * 
 * @author Michele La Porta
 *
 */
public class PresentationModel {	

	private static Logger logger = Logger.getLogger(PresentationModel.class);

	public static final int SINGLEPARTY_CALLTYPE = 1;
	public static final int TWOPARTIES_CALLTYPE = 2;
	public static final int CONFERENCE_CALLTYPE = 3;

	private AsteriskProvider asteriskProvider;
	private List<Call> singlePartyCalls;
	private List<Call> twoPartiesCalls;
	private List<Call> conferenceCalls;
	
	private List<PresentationModel.Listener> listeners;

	private int singlePartyCall_selectedIndex;
	private int twoPartiesCall_selectedIndex;
	private int conferenceCall_selectedIndex;
	
	public PresentationModel( AsteriskProvider asteriskProvider, List<Call> singlePartyCalls, 
		List<Call> twoPartiesCalls, List<Call> conferenceCalls) {
		
		if(asteriskProvider == null){ 
			logger.error("asteriskProvider null");
			throw new IllegalArgumentException("asteriskProvider can not be null");
		}
		if(singlePartyCalls == null){ 
			logger.error("singlePartyCalls null");
			throw new IllegalArgumentException("singlePartyCalls can not be null");
		}
		if(twoPartiesCalls == null){ 
			logger.error("twoPartiesCalls null");
			throw new IllegalArgumentException("twoPartiesCalls can not be null");
		}
		if(conferenceCalls == null){ 
			logger.error("conferenceCalls null");
			throw new IllegalArgumentException("conferenceCalls can not be null");
		}
		
		this.asteriskProvider = asteriskProvider;
		this.singlePartyCalls = singlePartyCalls;
		this.twoPartiesCalls = twoPartiesCalls;
		this.conferenceCalls = conferenceCalls;
		
		singlePartyCall_selectedIndex = -1;
		twoPartiesCall_selectedIndex = -1;
		conferenceCall_selectedIndex = -1;
		
		this.listeners = new ArrayList<PresentationModel.Listener>();
		
		if(logger.isDebugEnabled())
			logger.debug("PresentationModel ready " + this);
	}
	
	public void addListener(PresentationModel.Listener listener) {
		if(listener == null){ 
			logger.error("listener null");
			throw new IllegalArgumentException("listener can not be null");
		}
		listeners.add(listener);
		if(logger.isDebugEnabled())
			logger.debug("addListener " + listener);
	}
	
	public void removeListener(Listener listener) {
		if(listener == null) {
			logger.error("listener null");
			throw new IllegalArgumentException("listener can not be null");
		}
		listeners.remove(listener);
		if(logger.isDebugEnabled())
			logger.debug("removeListener " + listener);
	}
	
	public void callAttached(Call call) {
		
		for(Listener listener : listeners) {
			listener.callAttached(this, call);
			if(logger.isDebugEnabled())
			logger.debug("notify callAttached to " + listener);
		}
//		asteriskProvider.monitor(call);
		
	}
	
	public void callStateChanged(int oldState, Call call) {		
		for(Listener listener : listeners) {
			listener.callStateChanged(this, oldState, call);
			if(logger.isDebugEnabled())
			logger.debug("notify callStateChanged to " + listener);
		}
	}
	
	public void channelAdded(ConferenceCall conferenceCall, Channel channel) {
		if(logger.isDebugEnabled())
			logger.debug("channelAdded conferenceCall " + conferenceCall + " channel " + channel);
		for(Listener listener : listeners) {
			listener.channelAdded(this, conferenceCall, channel);
			if(logger.isDebugEnabled())
			logger.debug("channelAdded notify " + listener);
		}		
	}
	
	public void channelRemoved(ConferenceCall conferenceCall, Channel channel) {
		if(logger.isDebugEnabled())
			logger.debug("channelRemoved conferenceCall " + conferenceCall + " channel " + channel);
		for(Listener listener : listeners) {
			listener.channelRemoved(this, conferenceCall, channel);
			if(logger.isDebugEnabled())
			logger.debug("channelRemoved notify " + listener);
		}		
	}
	
	public void monitorButtonClicked(int callType) {
		validateCallType(callType);
		
		Call call = null;
		switch(callType) {
		case SINGLEPARTY_CALLTYPE:
			call = (Call) singlePartyCalls.get(singlePartyCall_selectedIndex);
			break;
		case TWOPARTIES_CALLTYPE:
			call = (Call) twoPartiesCalls.get(twoPartiesCall_selectedIndex);
			break;			
		case CONFERENCE_CALLTYPE:
			call = (Call) conferenceCalls.get(conferenceCall_selectedIndex);
			break;			
		}
		
		asteriskProvider.monitor(call);	
		if(logger.isDebugEnabled())
			logger.debug("monitorButtonClicked monitor " + call);
	}
	
	public void dropButtonClicked(int callType) {
		validateCallType(callType);
		
		Call call = null;
		switch(callType) {
		case SINGLEPARTY_CALLTYPE:
			call = (Call) singlePartyCalls.get(singlePartyCall_selectedIndex);
			break;
		case TWOPARTIES_CALLTYPE:
			call = (Call) twoPartiesCalls.get(twoPartiesCall_selectedIndex);
			break;			
		case CONFERENCE_CALLTYPE:
			call = (Call) conferenceCalls.get(conferenceCall_selectedIndex);
			break;			
		}
		
		asteriskProvider.drop(call); 
		if(logger.isDebugEnabled())
			logger.debug("dropButtonClicked drop " + call);
	}

	public boolean isMonitorButtonEnabled(int callType) {
		validateCallType(callType);
		int selectedIndex = getSelectedIndex(callType);
		if(selectedIndex > -1) {
			Call call = (Call) getCalls(callType).get(selectedIndex);
			return (call.getState() == Call.ACTIVE_STATE);
		}
		
		return false;
	}
	
	public boolean isDropButtonEnabled(int callType) {
		validateCallType(callType);		
		int selectedIndex = getSelectedIndex(callType);
		
		if(selectedIndex > -1) {
			Call call = (Call) getCalls(callType).get(selectedIndex);
			return (call.getState() == Call.ACTIVE_STATE);			
		}
		
		return false;
	}	
	
	public void setSelectedIndex(int callType, int selectedIndex) {
		validateCallType(callType);
		if(selectedIndex < -1) throw new IllegalArgumentException("selectedIndex must not be lower than -1");
		if(selectedIndex >= getCalls(callType).size()) {
			throw new IllegalArgumentException("selectedIndex must not be greater than " +
				"the size of the collection");
		}		
		
		switch(callType) {
		case SINGLEPARTY_CALLTYPE:
			singlePartyCall_selectedIndex = selectedIndex;
			break;
		case TWOPARTIES_CALLTYPE:
			twoPartiesCall_selectedIndex = selectedIndex;
			break;			
		case CONFERENCE_CALLTYPE:
			conferenceCall_selectedIndex = selectedIndex;
			break;			
		}
	}
	
	public int getSelectedIndex(int callType) {
		validateCallType(callType);
		
		switch(callType) {
		case SINGLEPARTY_CALLTYPE:
			return singlePartyCall_selectedIndex;
		case TWOPARTIES_CALLTYPE:
			return twoPartiesCall_selectedIndex;
		case CONFERENCE_CALLTYPE:
			return conferenceCall_selectedIndex;
		default:
			throw new AssertionError("This can not happen!");
		}		
	}
	
	public Call getSelectedCall(int callType) {
		validateCallType(callType);		
		return (Call) getCalls(callType).get(getSelectedIndex(callType));	
	}
	
	private void validateCallType(int callType) {
		if(!(callType == SINGLEPARTY_CALLTYPE || 
		     callType == TWOPARTIES_CALLTYPE ||
		     callType == CONFERENCE_CALLTYPE)) {
			throw new IllegalArgumentException("callType is not valid");
		}
	}
	
	public List<Call> getCalls(int callType) {
		switch(callType) {
		case SINGLEPARTY_CALLTYPE:
			return singlePartyCalls;
		case TWOPARTIES_CALLTYPE:
			return twoPartiesCalls;
		case CONFERENCE_CALLTYPE:
			return conferenceCalls;
		default:
			throw new AssertionError("Unknown callType " + callType);
		}		
	}	

	public void removeInvalidCalls() {
		if(singlePartyCalls.size() > 0) {
			for(int i = singlePartyCalls.size() - 1; i >= 0; i--) {
				Call call = (Call) singlePartyCalls.get(i);
				if(call.getState() == Call.INVALID_STATE) singlePartyCalls.remove(i);
			}
			setSelectedIndex(PresentationModel.SINGLEPARTY_CALLTYPE, -1);
			for(Listener listener : listeners) {
				listener.refreshTable(PresentationModel.SINGLEPARTY_CALLTYPE);
			}			
		}
		
		if(twoPartiesCalls.size() > 0) {
			for(int i = twoPartiesCalls.size() - 1; i >= 0; i--) {
				Call call = (Call) twoPartiesCalls.get(i);
				if(call.getState() == Call.INVALID_STATE) twoPartiesCalls.remove(i);
			}			
			setSelectedIndex(PresentationModel.TWOPARTIES_CALLTYPE, -1);
			for(Listener listener : listeners) {
				listener.refreshTable(PresentationModel.TWOPARTIES_CALLTYPE);
			}			
		}		
		
		if(conferenceCalls.size() > 0) {
			for(int i = conferenceCalls.size() - 1; i >= 0; i--) {
				Call call = (Call) conferenceCalls.get(i);
				if(call.getState() == Call.INVALID_STATE) conferenceCalls.remove(i);
			}			
			setSelectedIndex(PresentationModel.CONFERENCE_CALLTYPE, -1);
			for(Listener listener : listeners) {
				listener.refreshTable(PresentationModel.CONFERENCE_CALLTYPE);
			}			
		}	
	}
	
	/**
	 * AgiExp gui will implements to changes its tab info.
	 * @author Michele La Porta
	 *
	 */
	public static interface Listener {
		
		void callAttached(PresentationModel model, Call call);
		void callStateChanged(PresentationModel model, int oldState, Call call);
		void channelAdded(PresentationModel model, ConferenceCall conferenceCall, Channel channel);
		void channelRemoved(PresentationModel model, ConferenceCall conferenceCall, Channel channel);
		void refreshTable(int callType);
	}
	
}