package org.mik.asterisk.model.call.impl;

import java.util.Date;

import org.apache.log4j.Logger;
import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.ManagerEvent;
import org.mik.asterisk.model.call.Call;

public class SinglePartyCall extends Call {

	private static Logger logger = Logger.getLogger(SinglePartyCall.class);

	private Channel channel;
	
	public SinglePartyCall(String id, Date creationTime, int state, 
		Channel.Descriptor channelDescriptor) {
		super(id, creationTime, state);
		if(channelDescriptor == null) throw new IllegalArgumentException("channelDescriptor can not be null");
		this.channel = new Channel(channelDescriptor, this);
		logger.info(this);
	}
	
	public Channel getChannel() {
		return channel;
	}
	
	//for hibernate happiness
	Channel.Descriptor getChannelDescriptor() {
		return channel.getDescriptor();
	}	
	void setChannelDescriptor(Channel.Descriptor channelDescriptor) {
		this.channel = new Channel(channelDescriptor, this);
	}
	
    public boolean process(ManagerEvent event) {
    	if(getState() == Call.ACTIVE_STATE) {
    		if(event instanceof HangupEvent) {
    			HangupEvent he = (HangupEvent) event;
    			if(he.getChannel().equals(channel.getDescriptor().getId())) {
    				setState(Call.INVALID_STATE, "Call Ended");
    				logger.info("Call Ended");
    				return true;
    			}
    		}
    	}
    	
    	return false;
    }		
}