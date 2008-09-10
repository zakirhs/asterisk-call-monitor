package org.mik.asterisk.model.call;

import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import org.asteriskjava.manager.event.ManagerEvent;
import org.mik.asterisk.model.CallListener;
import org.mik.asterisk.model.Provider;

public abstract class Call {
	public static final int INVALID_STATE = -1;
	public static final int IDLE_STATE = 0;
	public static final int CONNECTING_STATE = 1;
	public static final int ACTIVE_STATE = 2;
	
	private String id;
	private Date creationTime;
	private Date invalidationTime;
	
	protected Provider provider;
	private int state;
	private String reasonForStateChange;
	
	protected Set listeners; 
	
	//for hibernate happiness
	Call() {
		state = INVALID_STATE;
	}
	
	public Call(String id, Date creationTime, int state) {		
		if(id == null) throw new IllegalArgumentException("id can not be null");
		if(creationTime == null) throw new IllegalArgumentException("creationTime can not be null");	
		
		this.id = id;
		this.creationTime = creationTime;
		this.state = state;
		
		listeners = new LinkedHashSet();
	}
	
	public Date getCreationTime() {
		return creationTime;
	}
	void setCreationTime(Date creationTime) {
		this.creationTime = creationTime;
	}
	
	public String getId() {
		return id;
	}
	void setId(String id) {
		this.id = id;
	}
	
	public Date getInvalidationTime() {
		return invalidationTime;
	}
	public void setInvalidationTime(Date invalidationTime) {
		this.invalidationTime = invalidationTime;
	}
	
	public int getState() {
		return state;
	}	
	protected void setState(int state, String reasonForStateChange) {
		int oldState = this.state;
		this.state = state;
		this.reasonForStateChange = reasonForStateChange;
		for(Iterator iter = new LinkedHashSet(listeners).iterator(); iter.hasNext();) {
			CallListener listener = (CallListener) iter.next();
			listener.stateChanged(oldState, this);
		}
	}
	
	public void setProvider(Provider provider) {
		if(provider == null) throw new IllegalArgumentException("provider can not be null");
		if(state == INVALID_STATE) throw new IllegalStateException("can not attach call in INVALID state.");
		this.provider = provider;
	}
	
	public void addListener(CallListener listener) {
		listeners.add(listener);
	}
	
	public void removeListener(CallListener listener) {
		listeners.remove(listener);
	}
	
    public boolean process(ManagerEvent event) {
    	return false;
    }	
    
    public String getReasonForStateChange() {
    	return reasonForStateChange;
    }
}