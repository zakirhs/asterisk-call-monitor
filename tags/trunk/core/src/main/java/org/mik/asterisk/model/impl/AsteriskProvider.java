package org.mik.asterisk.model.impl;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.action.HangupAction;
import org.asteriskjava.manager.action.MonitorAction;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.NewChannelEvent;
import org.mik.asterisk.model.CallListener;
import org.mik.asterisk.model.Provider;
import org.mik.asterisk.model.ProviderListener;
import org.mik.asterisk.model.call.Call;
import org.mik.asterisk.model.call.CallConstruction;
import org.mik.asterisk.model.call.impl.Channel;
import org.mik.asterisk.model.call.impl.ConferenceCall;
import org.mik.asterisk.model.call.impl.SinglePartyCall;
import org.mik.asterisk.model.call.impl.TwoPartiesCall;

public class AsteriskProvider implements Provider, ManagerEventListener, CallListener {

	private static Logger logger = Logger.getLogger(AsteriskProvider.class);

	private Set attachedCalls;
	private Set listeners;
	private Set callConstructions;
	private ManagerConnection managerConnection;
	
	public AsteriskProvider(ManagerConnection managerConnection) {
		if(managerConnection == null) throw new IllegalArgumentException("managerConnection can not be null");
		
		this.managerConnection = managerConnection;
		
		attachedCalls = new LinkedHashSet();
		callConstructions = new LinkedHashSet();
		listeners = new LinkedHashSet();
		logger.info(this);
	}
	
	public final void attachCall(Call call) {
		call.setProvider(this);
		attachedCalls.add(call);
		System.out.println("call attached: " + call);
		
		for(Iterator iter = listeners.iterator(); iter.hasNext(); ) {
			ProviderListener listener = (ProviderListener) iter.next();
			listener.callAttached(call);
		}
		
		call.addListener(this);
	}

	public void onManagerEvent(ManagerEvent event) {
		logger.info("onManagerEvent " + event);
		boolean processed = false;
		logger.info("attachedCalls " + attachedCalls.size());
		
		for(Iterator iter = attachedCalls.iterator(); iter.hasNext();) {
			Call call = (Call) iter.next();
			processed = call.process(event);
			logger.info("processEvent Call processed " + processed);
			if(processed) break;
		}   
		if(!processed) processEvent(event);
	}
	
	private void processEvent(ManagerEvent event) {
		boolean processed = false;

		for(Iterator iter = callConstructions.iterator(); iter.hasNext();) {
			CallConstruction callConstruction = (CallConstruction) iter.next();
			processed = callConstruction.process(event);
			logger.info("processEvent CallConstruction processed " + processed);
			if(processed) break;
		}	
		
		if(!processed) {
			if(event instanceof NewChannelEvent) {
				NewChannelEvent nce = (NewChannelEvent) event;				
				if("Down".equals(nce.getState())) {
					CallConstruction callConstruction = new CallConstruction(this, nce);
					callConstructions.add(callConstruction);
				}
			}else{
				logger.info("processEvent NOT processed " + event);
			}
		}
			
					
	}

	public void removeCallConstruction(CallConstruction callConstruction) {
		callConstructions.remove(callConstruction);		
	}

	public Set getAttachedCalls() {		
		return new LinkedHashSet(attachedCalls);
	}

	public void addListener(ProviderListener listener) {
		listeners.add(listener);
	}

	public void removeListener(ProviderListener listener) {
		listeners.remove(listener);
	}

	public void drop(Call call) {
		if(call instanceof SinglePartyCall) {
			SinglePartyCall spc = (SinglePartyCall) call;
			HangupAction hangupAction = new HangupAction(spc.getChannel().getDescriptor().getId());
			try {
				managerConnection.sendAction(hangupAction);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} 
		} else if(call instanceof TwoPartiesCall) {
			TwoPartiesCall tpc = (TwoPartiesCall) call;
			HangupAction hangupAction = new HangupAction(tpc.getDialingChannel().getDescriptor().getId());
			try {
				managerConnection.sendAction(hangupAction);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}			
		} else if(call instanceof ConferenceCall) {
			ConferenceCall cc = (ConferenceCall) call;
			for(Iterator iter = cc.getChannels().iterator(); iter.hasNext();) {
				Channel channel = (Channel) iter.next();
				HangupAction hangupAction = new HangupAction(channel.getDescriptor().getId());
				try {
					managerConnection.sendAction(hangupAction);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}				
			}
		}
	}

	public void stateChanged(int oldState, Call call) {
		if(call.getState() == Call.INVALID_STATE) {
			System.out.println("call detached: " + call);
			attachedCalls.remove(call);
			for(Iterator iter = listeners.iterator(); iter.hasNext(); ) {
				ProviderListener listener = (ProviderListener) iter.next();
				listener.callDetached(call);
			}			
		}
	}
	public void channelAdded(ConferenceCall conferenceCall, Channel channel) {}
	public void channelRemoved(ConferenceCall conferenceCall, Channel channel) {}

	public void monitor(Call call) {		
		String monitoredChannel = null;
		if(call instanceof SinglePartyCall) {
			SinglePartyCall spc = (SinglePartyCall) call;
			monitoredChannel = spc.getChannel().getDescriptor().getId();
		} else if(call instanceof TwoPartiesCall) {
			TwoPartiesCall tpc = (TwoPartiesCall) call;
			monitoredChannel = tpc.getDialingChannel().getDescriptor().getId();		
		} else if(call instanceof ConferenceCall) {
			ConferenceCall cc = (ConferenceCall) call;			
			for(Iterator iter = cc.getChannels().iterator(); iter.hasNext();) {
				Channel channel = (Channel) iter.next();
				String channelId = channel.getDescriptor().getId(); 
				if(channelId.startsWith("SIP/notetaker-")) {
					monitoredChannel = channelId;		
					break;
				}
			}
		}
		
		System.out.println("monitoredChannel:" + monitoredChannel);
		StringBuffer fileName = new StringBuffer();
		if(call instanceof SinglePartyCall) {
			SinglePartyCall spc = (SinglePartyCall) call;
			fileName.append("singleparty_")
				.append(spc.getId());			
		} else if(call instanceof TwoPartiesCall) {
			TwoPartiesCall tpc = (TwoPartiesCall) call;
			fileName.append("twoparties_")
				.append(tpc.getId());			
		} else if(call instanceof ConferenceCall) {
			ConferenceCall cc = (ConferenceCall) call;
			fileName.append("conference_")
				.append(cc.getId());			
		}
		
		MonitorAction monitorAction = new MonitorAction(monitoredChannel, 
				"/var/recordings/" + fileName.toString(), "wav", Boolean.TRUE);
		try {
			managerConnection.sendAction(monitorAction);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}	
}