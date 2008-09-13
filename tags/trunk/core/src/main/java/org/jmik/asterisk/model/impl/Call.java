package org.jmik.asterisk.model.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.log4j.Logger;
import org.asteriskjava.manager.event.ManagerEvent;
import org.jmik.asterisk.model.CallListener;
import org.jmik.asterisk.model.ProviderListener;
import org.jmik.asterisk.model.impl.Channel.Descriptor;

/**
 * This class rappresents a telephone call,the information flowing
 * between the service provider and the call participants.
 * The information flow between a call participant and the provider through
 * an its logical conduct (Channel).
 * 
 * @author Michele La Porta
 *
 */
public abstract class Call{

	protected static Logger logger = Logger.getLogger(Call.class);

	public static final int SINGLEPARTY_CALL = 0;
	public static final int TWOPARTIES_CALL = 1;
	public static final int CONFERENCE_CALL = 2;

     

	// This is the initial state for all Calls. In this state, the Call Call has zero Connections.
	public static final int IDLE_STATE = 0;

	//public static final int IN_PROGRESS = 0;
	//public static final int ALERT = 0;

	public static final int CONNECTING_STATE = 1;
	
	// A Call with some current ongoing activity is in this state.
	public static final int ACTIVE_STATE = 2;

	// RINGING --> ACTIVE / PASSIVE --> DROPPED
	// MEDIA ENDPOINT HELD / CLOSED

	// This is the final state for all Calls
	public static final int INVALID_STATE = 3;

	protected int state;
	protected int reasonForStateChange;

	protected String id;
	protected java.util.Date creationTime;
	protected java.util.Date invalidationTime;
	protected Channel channel;
	
	protected Set<CallListener> listeners;
	protected Set<ProviderListener> providerListeners;
	//java.util.concurrent.CopyOnWriteArraySet
	private ReadWriteLock globalLock;
	private Lock readLock;
	private Lock writeLock;

	public Call(String callId,Date date,int state,Descriptor descriptor) {
		
		listeners = new HashSet<CallListener>();
		providerListeners = new HashSet<ProviderListener>();
		globalLock = new ReentrantReadWriteLock();
		readLock = globalLock.readLock();
		writeLock = globalLock.writeLock();
		
		channel = new Channel(descriptor);
		creationTime = date;
		this.id = callId;
		this.state = state;
		logger.info("Call state " + this.state);
	}

	public abstract boolean process(ManagerEvent event);

	protected abstract void setState(int state,String reasonForStateChange) ;

	public void stateChanged(int oldState,Call call){
		writeLock.lock();
		try{
			logger.info("stateChanged " + call + " oldState " + oldState + " newState " + call.getState());
			CopyOnWriteArraySet<CallListener> copyOnWriteArraySet = new CopyOnWriteArraySet<CallListener>(listeners);
			for(CallListener callListener : copyOnWriteArraySet){
				callListener.stateChanged(oldState, call);
			}
			/*if(call.getState() == Call.INVALID_STATE){
				CopyOnWriteArraySet<ProviderListener> copyOnWriteArraySet2 = new CopyOnWriteArraySet<ProviderListener>(providerListeners);
				
				for(ProviderListener providerListener : copyOnWriteArraySet2){
					providerListener.callDetached(call);
				}
			}*/
			
		}finally {
			writeLock.unlock();
		}
	}

	public String getId() {
		return id;
	}

	public java.util.Date getCreationTime() {
		return creationTime;
	}

	public java.util.Date getInvalidationTime() {
		return invalidationTime;
	}

	public void setInvalidationTime(java.util.Date invalidationTime) {
		this.invalidationTime = invalidationTime;
	}

	public int getState() {
		return state;
	}

	public int getReasonForStateChange() {
		return reasonForStateChange;
	}

	public void setReasonForStateChange(int reasonForStateChange) {
		this.reasonForStateChange = reasonForStateChange;
	}

	public Channel getChannel() {
		return channel;
	}

	public Set<CallListener> getListeners() {
		readLock.lock();
		try {
			return this.listeners;
		} finally {
			readLock.unlock();
		}
	}

	public void addListener(CallListener listener) {
		writeLock.lock();
		try {
			listeners.add(listener);
			logger.info("addListener " + listeners);
		} finally {
			writeLock.unlock();
		}
	}

	public void removeListener(CallListener listener){
		writeLock.lock();
		try {
			listeners.remove(listener);
			logger.info("removeListener " + listeners);
		} finally {
			writeLock.unlock();
		}
	}

	@Override
	public String toString() {
		StringBuffer st = new StringBuffer()
		.append(this.getClass().getSimpleName()+"@"+this.hashCode()+ "[channel="+channel.getDescriptor().getId() +",state="+state+",reasonForStateChange=" + reasonForStateChange + ",listeners="+listeners+"]");
		return st.toString();
	}
	
	@Override
	public int hashCode() {
		return super.hashCode();
	}
}
