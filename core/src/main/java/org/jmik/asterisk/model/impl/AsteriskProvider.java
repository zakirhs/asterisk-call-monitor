package org.jmik.asterisk.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.action.HangupAction;
import org.asteriskjava.manager.action.MonitorAction;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.NewChannelEvent;
import org.jmik.asterisk.model.CallListener;
import org.jmik.asterisk.model.Provider;
import org.jmik.asterisk.model.ProviderListener;

/**
 * This class represents the implementation of a Provider.
 * It holds instances of active (attached) Call.
 * 
 * @author Michele La Porta
 *
 */
public class AsteriskProvider implements Provider,ManagerEventListener,CallListener{

	private static Logger logger = Logger.getLogger(AsteriskProvider.class);

	private Set<Call> attachedCalls;
	private Set<CallCostruction> callConstrutions;
	private List<ProviderListener> listeners;
	
    private ManagerConnection managerConnection;
    
    /**
     * 
     * @param managerConnection
     */
    public AsteriskProvider(ManagerConnection managerConnection){
    	this.managerConnection = managerConnection;
    	attachedCalls = new LinkedHashSet<Call>();
    	callConstrutions = new LinkedHashSet<CallCostruction>();
    	listeners = new ArrayList<ProviderListener>();
    }
    
	public void onManagerEvent(ManagerEvent managerEvent){
		
		logger.debug("RECEIVED " + managerEvent);
		
		boolean processed = false;
		
		logger.debug("attachedCalls " +attachedCalls.size());
		// iterate through attached Calls if any "accept" the managerEvent
		for (Call call : this.attachedCalls) {
			processed = call.process(managerEvent);
			logger.debug("" + call.getClass().getSimpleName() +"[state=" + call.getState()+ "] processEvent " + managerEvent.getClass().getSimpleName() + " processed " + processed);
			
			if(processed)break;
		}
		
		if(!processed) processEvent(managerEvent);
		
	}
	
	/**
	 * 
	 * @param managerEvent
	 */
	private void processEvent(ManagerEvent event){
		
		boolean processed = false;
		
		// iterate through the living CallConstruction
		for(CallCostruction callCostruction : callConstrutions){
			processed = callCostruction.processEvent(event);
			logger.info("processEvent " + event.getClass().getSimpleName() +" cc state " + callCostruction.getWaitState() + " processed " + processed);
			
			if(processed) break;
		}

		if(!processed){
			// inspect type of Event
			if(event instanceof NewChannelEvent) {
				NewChannelEvent newChannelEvent = (NewChannelEvent) event;
				if("Down".equals(newChannelEvent.getState())) {
					CallCostruction callCostruction = new CallCostruction(this,newChannelEvent);
					callConstrutions.add(callCostruction);
				}
			}else{
				logger.info("NOT processed " + event);
			}
		}
	}

	public void removeCallConstruction(CallCostruction callConstruction) {
		callConstrutions.remove(callConstruction);
		logger.info("removeCallConstruction " + callConstruction);
	}

	public void addListener(ProviderListener providerListener) {
		listeners.add(providerListener);
		logger.info("addListener " + listeners);
	}
	
	public void removeListener(ProviderListener providerListener) {
		listeners.remove(providerListener);
		logger.info("removeListener " + listeners);
	}

	public void attachCall(Call call) {
		logger.info("attachCall  " + attachedCalls);
		logger.info("attachCall  " + call);
		
		this.attachedCalls.add(call);
		logger.info("attachedCalls size " + attachedCalls.size() + " " + attachedCalls);
		logger.info("listeners size " + listeners.size() + " " + listeners);
		
		for(ProviderListener providerListener : listeners){
			providerListener.callAttached(call);
		}
	}

	public void detachCall(Call call) {
		this.attachedCalls.remove(call);
		logger.info("detachCall size " + attachedCalls.size() + " " + attachedCalls);
		for(ProviderListener providerListener : listeners){
			providerListener.callDetached(call);
		}
	}

	public Set<Call> getAttachedCalls() {
		return this.attachedCalls;
	}

//	public void monitor(Call call) {
//		if(call instanceof SinglePartyCall){
//			logger.info("monitor " + call);
//		}else if(call instanceof TwoPartiesCall){
//			logger.info("monitor " + call);
//		}else if(call instanceof ConferenceCall){
//			logger.info("monitor " + call);
//		}
//	}
//
//	public void drop(Call call) {
//		if(call instanceof SinglePartyCall){
//			SinglePartyCall singlePartyCall = (SinglePartyCall)call;
//			HangupAction hangupAction = new HangupAction(singlePartyCall.getChannel().getDescriptor().getId());
//			try {
//				managerConnection.sendAction(hangupAction);
//			} catch (Exception e) {
//				throw new RuntimeException();
//			}
//			logger.info("drop " + singlePartyCall);
//						
//		}else if(call instanceof TwoPartiesCall){
//			
//			TwoPartiesCall twoPartiesCall = (TwoPartiesCall)call;
//			HangupAction hangupAction = new HangupAction(twoPartiesCall.getCallerChannel().getDescriptor().getId());
//			try {
//				managerConnection.sendAction(hangupAction);
//			} catch (Exception e) {
//				throw new RuntimeException();
//			}
//			logger.info("drop " + twoPartiesCall);
//			
//		}else if(call instanceof ConferenceCall){
//			
//			ConferenceCall conferenceCall = (ConferenceCall)call;
//			for (Iterator<Channel> iterator = conferenceCall.getChannels().iterator(); iterator.hasNext();) {
//				Channel channel = (Channel) iterator.next();
//				HangupAction hangupAction = new HangupAction(channel.getDescriptor().getId());
//				try {
//					managerConnection.sendAction(hangupAction);
//				} catch (Exception e) {
//					throw new RuntimeException();
//				}
//			}
//			logger.info("drop " + conferenceCall);
//			
//		}
//		this.attachedCalls.remove(call);
//		logger.info("remove " +call+" from attachedCalls " + attachedCalls);
//		
//	}

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
			HangupAction hangupAction = new HangupAction(tpc.getCallerChannel().getDescriptor().getId());
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
	public void monitor(Call call) {	
		logger.info("monitor " +call);
		
		String monitoredChannel = null;
		if(call instanceof SinglePartyCall) {
			SinglePartyCall spc = (SinglePartyCall) call;
			monitoredChannel = spc.getChannel().getDescriptor().getId();
		} else if(call instanceof TwoPartiesCall) {
			TwoPartiesCall tpc = (TwoPartiesCall) call;
			monitoredChannel = tpc.getCallerChannel().getDescriptor().getId();		
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
	public Set<CallCostruction> getCallConstrutions() {
		return callConstrutions;
	}

	public List<ProviderListener> getListeners() {
		return listeners;
	}

	public ManagerConnection getManagerConnection() {
		return managerConnection;
	}

}
