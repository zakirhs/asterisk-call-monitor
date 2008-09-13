package org.mik.asterisk.model.call.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.MeetMeLeaveEvent;
import org.mik.asterisk.model.CallListener;
import org.mik.asterisk.model.call.Call;
import org.mik.asterisk.model.utils.DateFormatter;

public class ConferenceCall extends Call {

	private static Logger logger = Logger.getLogger(ConferenceCall.class);

	private String roomId;
	private Map channels;	
	
	public ConferenceCall(String roomId, Date creationTime, Channel.Descriptor channelDesc) {		
		super(roomId + "|" + DateFormatter.format(channelDesc.getCreationTime()), creationTime, Call.ACTIVE_STATE);
		if(roomId == null) throw new IllegalArgumentException("roomId can not be null");
		if(channelDesc == null) throw new IllegalArgumentException("channelDesc can not be null");
		
		this.roomId = roomId;
		channels = new HashMap();
		addChannel(channelDesc);
		logger.info(this);
	}
	
	public String getRoomId() {
		return roomId;
	}
	
	public Set getChannels() {
		return new LinkedHashSet(channels.values());
	}
	
	//notification -- when a party joins the conference 
	public void addChannel(Channel.Descriptor channelDesc) {
		if(channelDesc == null) throw new AssertionError("channelDesc can not be null");
		Channel channel = new Channel(channelDesc, this);
		channels.put(channelDesc, channel);
		logger.info("add channel "+ channelDesc.getEndpoint().getId());
		
		for(Iterator iter = listeners.iterator(); iter.hasNext();) {
			CallListener listener = (CallListener) iter.next();
			listener.channelAdded(this, channel);
		}
	}
	//notification -- when a party leaves the conference 
	void removeChannel(Channel.Descriptor channelDesc) {
		if(channelDesc == null) throw new AssertionError("channel can not be null");
		if(channels.containsKey(channelDesc)) {
			Channel channel = (Channel) channels.remove(channelDesc);
			logger.info("remove channel "+ channelDesc.getEndpoint().getId());
			
			for(Iterator iter = listeners.iterator(); iter.hasNext();) {
				CallListener listener = (CallListener) iter.next();
				listener.channelRemoved(this, channel);
			}	
		}
		
		if(channels.size() == 0) {
			setState(Call.INVALID_STATE, "All Parties Left");	
			logger.info("All Parties Left");
			
		}
	}
	
    public boolean process(ManagerEvent event) {
    	if(getState() == Call.ACTIVE_STATE) {
    		if(event instanceof MeetMeLeaveEvent) {
    			MeetMeLeaveEvent mmle = (MeetMeLeaveEvent) event;
    			for(Iterator iter = channels.keySet().iterator(); iter.hasNext();) {
    				Channel.Descriptor channelDesc = (Channel.Descriptor) iter.next();
    				if(channelDesc.getId().equals(mmle.getChannel())) {
    					removeChannel(channelDesc);
    					return true;
    				}
    			}
    		}
    	}
    	
    	return false;
    }
}