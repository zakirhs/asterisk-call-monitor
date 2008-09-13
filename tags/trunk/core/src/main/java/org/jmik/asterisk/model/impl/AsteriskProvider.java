package org.jmik.asterisk.model.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.action.HangupAction;
import org.asteriskjava.manager.event.ConnectEvent;
import org.asteriskjava.manager.event.DisconnectEvent;
import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.NewChannelEvent;
import org.asteriskjava.manager.event.ShutdownEvent;
import org.jmik.asterisk.model.Provider;
import org.jmik.asterisk.model.ProviderListener;

/**
 * This class represents the implementation of a Provider.
 * It holds instances of active (attached) Call.
 * 
 * @author Michele La Porta
 *
 */
public class AsteriskProvider implements Provider{

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
    	attachedCalls = new HashSet<Call>();
    	callConstrutions = new HashSet<CallCostruction>();
    	listeners = new ArrayList<ProviderListener>();;
    }
    
	public void onManagerEvent(ManagerEvent managerEvent){
		
		if(logger.isInfoEnabled())
			logger.info("onManagerEvent CALLED " +managerEvent);
		
		boolean processed = false;
		
		// iterate through attached Calls if any "accept" the managerEvent
		for (Call call : this.attachedCalls) {
			processed = call.process(managerEvent);
			// if processed?
			if(call.getState() == Call.INVALID_STATE &&
				managerEvent instanceof HangupEvent){
					logger.info("providerListener size " + listeners.size() + " " + listeners);
					for(ProviderListener providerListener : listeners){
						providerListener.callDetached(call);
					}
				}
			
			if(processed)break;
		}
		
		if(!processed) processEvent(managerEvent);
		
	}
	
	/**
	 * 
	 * @param managerEvent
	 */
	private void processEvent(ManagerEvent managerEvent){
		
		boolean processed = false;
		
		// iterate through the living CallConstruction
		for(CallCostruction callCostruction : callConstrutions){
			processed = callCostruction.processEvent(managerEvent);
			
			if(processed) break;
		}

		if(!processed){
			// inspect type of Event
			if(managerEvent instanceof NewChannelEvent){
				NewChannelEvent newChannelEvent = (NewChannelEvent) managerEvent;
				if(!newChannelEvent.getState().equals("Rsrvd")){
					CallCostruction newCallCostruction = new CallCostruction(this,newChannelEvent);
					callConstrutions.add(newCallCostruction);
					logger.info("instantiate new CallCostruction");
				}
			}else if(managerEvent instanceof ConnectEvent){
				ConnectEvent connectEvent = (ConnectEvent) managerEvent;
				logger.info("READY:" + connectEvent.getProtocolIdentifier());
			}else if(managerEvent instanceof ShutdownEvent){
				ShutdownEvent shutdownEvent = (ShutdownEvent)managerEvent;
				logger.info("SHUTDOWN:" + shutdownEvent.getShutdown()+" Restart:" + shutdownEvent.getRestart());
			}else if(managerEvent instanceof DisconnectEvent){
				DisconnectEvent disconnectEvent = (DisconnectEvent)managerEvent;
				logger.info("DISCONNECT:" + disconnectEvent.getTimestamp());
			}
			
		}
	}

	public void removeCallConstruction(CallCostruction callConstruction) {
		callConstrutions.remove(callConstruction);
		logger.info("removeCallConstruction " + callConstrutions);
	}

	public void addListener(ProviderListener providerListener) {
		listeners.add(providerListener);
//		logger.info("addListener " + listeners);
	}
	
	public void removeListener(ProviderListener providerListener) {
		listeners.remove(providerListener);
//		logger.info("removeListener " + listeners);
	}

	public void attachCall(Call call) {
		this.attachedCalls.add(call);
		logger.info("attachedCalls size " + attachedCalls.size() + " " + attachedCalls);
		
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

	public void monitor(Call call) {
		if(call instanceof SinglePartyCall){
			logger.info("monitor " + call);
		}else if(call instanceof TwoPartiesCall){
			logger.info("monitor " + call);
		}else if(call instanceof ConferenceCall){
			logger.info("monitor " + call);
		}
	}

	public void drop(Call call) {
		if(call instanceof SinglePartyCall){
			SinglePartyCall singlePartyCall = (SinglePartyCall)call;
			HangupAction hangupAction = new HangupAction(singlePartyCall.getChannel().getDescriptor().getId());
			try {
				managerConnection.sendAction(hangupAction);
			} catch (Exception e) {
				throw new RuntimeException();
			}
			logger.info("drop " + singlePartyCall);
						
		}else if(call instanceof TwoPartiesCall){
			
			TwoPartiesCall twoPartiesCall = (TwoPartiesCall)call;
			HangupAction hangupAction = new HangupAction(twoPartiesCall.getCallerChannel().getDescriptor().getId());
			try {
				managerConnection.sendAction(hangupAction);
			} catch (Exception e) {
				throw new RuntimeException();
			}
			logger.info("drop " + twoPartiesCall);
			
		}else if(call instanceof ConferenceCall){
			
			ConferenceCall conferenceCall = (ConferenceCall)call;
			for (Iterator<Channel> iterator = conferenceCall.getChannels().iterator(); iterator.hasNext();) {
				Channel channel = (Channel) iterator.next();
				HangupAction hangupAction = new HangupAction(channel.getDescriptor().getId());
				try {
					managerConnection.sendAction(hangupAction);
				} catch (Exception e) {
					throw new RuntimeException();
				}
			}
			logger.info("drop " + conferenceCall);
			
		}
		this.attachedCalls.remove(call);
		logger.info("remove " +call+" from attachedCalls " + attachedCalls);
		
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
