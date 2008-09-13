package org.mik.asterisk.model.call.impl;

import java.util.Date;

import org.apache.log4j.Logger;
import org.asteriskjava.manager.event.DialEvent;
import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.LinkEvent;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.NewChannelEvent;
import org.asteriskjava.manager.event.UnlinkEvent;
import org.mik.asterisk.model.call.Call;

public class TwoPartiesCall extends Call {
	
	private static Logger logger = Logger.getLogger(TwoPartiesCall.class);
	
	private Channel dialingChannel;
	private Channel dialedChannel;	
	
	public TwoPartiesCall(String id, Date creationTime, Channel.Descriptor dialingChannelDescriptor, 
		Channel.Descriptor dialedChannelDescriptor) {
		super(id, creationTime, Call.IDLE_STATE);
		if(dialingChannelDescriptor == null) throw new IllegalArgumentException("dialingChannelDescriptor can not be null");
		if(dialedChannelDescriptor == null) throw new IllegalArgumentException("dialedChannelDescriptor can not be null");
		
		this.dialingChannel = new Channel(dialingChannelDescriptor, this);
		this.dialedChannel = new Channel(dialedChannelDescriptor, this);
		logger.info(this);
	}
	
	public Channel getDialingChannel() {
		return dialingChannel;
	}	
	public Channel getDialedChannel() {
		return dialedChannel;
	}
	
	//for hibernate happiness
	Channel.Descriptor getDialingChannelDescriptor() {
		return dialingChannel.getDescriptor();
	}
	void setDialingChannelDescriptor(Channel.Descriptor dialingChannelDescriptor) {
		this.dialingChannel = new Channel(dialingChannelDescriptor, this);
	}
	Channel.Descriptor getDialedChannelDescriptor() {
		return dialedChannel.getDescriptor();
	}
	void setDialedChannelDescriptor(Channel.Descriptor dialedChannelDescriptor) {
		this.dialedChannel = new Channel(dialedChannelDescriptor, this);
	}	
	
    public boolean process(ManagerEvent event) {    	
    	switch(getState()) {
    	case Call.IDLE_STATE:
    		if(event instanceof DialEvent) {
    			DialEvent de = (DialEvent) event;
    			if(de.getSrc().equals(dialingChannel.getDescriptor().getId())) {
    				dialedChannel.getDescriptor().setId(de.getDestination());
    				setState(Call.CONNECTING_STATE, "Dialing");
    				logger.info("Dialing");
    				return true;
    			} 
    		} else if(event instanceof HangupEvent) {
    			HangupEvent he = (HangupEvent) event;
    			if(he.getChannel().equals(dialingChannel.getDescriptor().getId())) {
    				setState(Call.INVALID_STATE, "No Route");
    				logger.info("No Route");
    				return true;
    			}
    		}
    		break;
    	case Call.CONNECTING_STATE:
    		if(event instanceof NewChannelEvent) {
        		NewChannelEvent nce = (NewChannelEvent) event;
        		if(nce.getChannel().equals(dialedChannel.getDescriptor().getId())) {        			
        			setState(Call.CONNECTING_STATE, "Ringing");
        			logger.info("Ringing");
    				return true;
        		}
        	} else if(event instanceof HangupEvent) {
        		HangupEvent he = (HangupEvent) event;
        		if(he.getChannel().equals(dialingChannel.getDescriptor().getId()) || 
        		   he.getChannel().equals(dialedChannel.getDescriptor().getId())) {
        			if(he.getCause().intValue() == 16) {
        				setState(Call.INVALID_STATE, "Canceled");
        				logger.info("Canceled");
        				return true;        			
            		} else if(he.getCause().intValue() == 21) {
        				setState(Call.INVALID_STATE, "Rejected");
        				logger.info("Rejected");
        				return true;        			
            		}        			
        		} 
    		} else if(event instanceof LinkEvent) {
    			LinkEvent le = (LinkEvent) event;
    			if(le.getChannel1().equals(dialingChannel.getDescriptor().getId())) {
    				setState(Call.ACTIVE_STATE, "Answered");
    				logger.info("Answered");
    				return true;
    			}	
    		}
    		break;
    	case Call.ACTIVE_STATE:
    		if(event instanceof UnlinkEvent) {
    			UnlinkEvent ue = (UnlinkEvent) event;
    			if(ue.getChannel1().equals(dialingChannel.getDescriptor().getId())) {
    				setState(Call.INVALID_STATE, "Call Ended");
    				logger.info("Call Ended");
    				return true;
    			}
    		} 
    		break;
    	}	

    	return false;
    }
}