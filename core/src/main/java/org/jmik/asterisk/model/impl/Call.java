package org.jmik.asterisk.model.impl;

import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

import org.apache.log4j.Logger;
import org.asteriskjava.manager.event.ManagerEvent;
import org.jmik.asterisk.model.CallListener;
import org.jmik.asterisk.model.Provider;

/**
 * This class rappresents a telephone call,the information flowing
 * between the service provider and the call participants.
 * The information flow between a call participant and the provider through
 * an its logical conduct (Channel).
 * 
 * @author Michele La Porta
 *
 */
public class Call{

	protected static Logger logger = Logger.getLogger(Call.class);

//	public static final int SINGLEPARTY_CALL = 0;
//	public static final int TWOPARTIES_CALL = 1;
//	public static final int CONFERENCE_CALL = 2;

	public static final int IDLE_STATE = 0;
	public static final int CONNECTING_STATE = 1;
	public static final int ACTIVE_STATE = 2;
	public static final int INVALID_STATE = -1;

	protected String id;
	protected Provider provider;
	
	protected java.util.Date creationTime;
	protected java.util.Date invalidationTime;
	protected int state;
	protected String reasonForStateChange;
	
	protected Channel channel;
	
	protected Set<CallListener> listeners;
//	protected Set<ProviderListener> providerListeners;
	//java.util.concurrent.CopyOnWriteArraySet
//	private ReadWriteLock globalLock;
//	private Lock readLock;
//	private Lock writeLock;
	
	Call() {
		state = INVALID_STATE;
	}


	public Call(String id, Date creationTime, int state) {

		logger.info("id " +id);
		logger.info("creationTime " + creationTime);

		if (id == null){
			logger.error("id is null");
			throw new IllegalArgumentException("id cannot be null");
		}
		if (creationTime == null){
			logger.error("creationTime is null");
			throw new IllegalArgumentException("creationTime cannot be null");
		}
		
		listeners = new LinkedHashSet<CallListener>();
//		providerListeners = new HashSet<ProviderListener>();
//		globalLock = new ReentrantReadWriteLock();
//		readLock = globalLock.readLock();
//		writeLock = globalLock.writeLock();
		
//		channel = new Channel(descriptor);
		this.creationTime = creationTime;
		this.id = id;
		this.state = state;
		logger.info("Call " + this + "state " + this.state);
	}

	public void setProvider(Provider provider) {
		if(provider == null) throw new IllegalArgumentException("provider can not be null");
		if(state == INVALID_STATE) throw new IllegalStateException("can not attach call in INVALID state.");
		this.provider = provider;
	}


	protected void setState(int state, String reasonForStateChange) {
		int oldState = this.state;
		this.state = state;
		this.reasonForStateChange = reasonForStateChange;
		logger.info("setState listeners size " + listeners.size() + " " + listeners);
		for(CallListener callListener : new LinkedHashSet<CallListener>(listeners))
			callListener.stateChanged(oldState, this);
	}

	public boolean process(ManagerEvent event) {
    	return false;
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

	public String getReasonForStateChange() {
		return reasonForStateChange;
	}

	public void setReasonForStateChange(String reasonForStateChange) {
		this.reasonForStateChange = reasonForStateChange;
	}

	public Channel getChannel() {
		return channel;
	}

//	public Set<CallListener> getListeners() {
//		readLock.lock();
//		try {
//			return this.listeners;
//		} finally {
//			readLock.unlock();
//		}
//	}

	public void addListener(CallListener listener) {
//		writeLock.lock();
//		try {
			listeners.add(listener);
			logger.info("addListener " + listeners);
//		} finally {
//			writeLock.unlock();
//		}
	}

	public void removeListener(CallListener listener){
//		writeLock.lock();
//		try {
			listeners.remove(listener);
			logger.info("removeListener " + listeners);
//		} finally {
//			writeLock.unlock();
//		}
	}

//	@Override
//	public String toString() {
//		StringBuffer st = new StringBuffer()
//		.append(this.getClass().getSimpleName()+"@"+this.hashCode()+ "[channel="+channel.getDescriptor().getId() +",state="+state+",reasonForStateChange=" + reasonForStateChange + ",listeners="+listeners+"]");
//		return st.toString();
//	}
//	
//	@Override
//	public int hashCode() {
//		return super.hashCode();
//	}
}
