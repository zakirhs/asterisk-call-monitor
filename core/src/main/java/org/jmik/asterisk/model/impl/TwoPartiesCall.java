package org.jmik.asterisk.model.impl;

import java.util.Date;

import org.apache.log4j.Logger;
import org.asteriskjava.manager.event.DialEvent;
import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.LinkEvent;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.NewChannelEvent;
import org.asteriskjava.manager.event.UnlinkEvent;

/**
 * This class represents a call with two participant (thus two channels) 
 * just like a normal phone call.
 * 
 * @author Michele La Porta
 *
 */
public class TwoPartiesCall extends Call{

	private static Logger logger = Logger.getLogger(TwoPartiesCall.class);

	private Channel callerChannel;
	private Channel calledChannel;

	public TwoPartiesCall(String callId,Date date,Channel.Descriptor dialing,Channel.Descriptor dialed){
		super(callId, date, Call.IDLE_STATE);

		if(dialing == null){ 
			logger.info("callerChannel " + callerChannel);
			throw new IllegalArgumentException("dialingChannelDescriptor can not be null");
		}
		if(dialed == null){ 
			logger.info("calledChannel " + calledChannel);
			throw new IllegalArgumentException("dialedChannelDescriptor can not be null");
		}
		
		callerChannel = new Channel(dialing,this);
		calledChannel = new Channel(dialed,this);
		logger.info("IDLE " + this);
	}
	
	public boolean process(ManagerEvent event){
		logger.info("STATE " + this.getState() + " process event " +event);
		
		switch (state) {
		
			case Call.IDLE_STATE:
				if(event instanceof DialEvent){
					DialEvent dialEvent = (DialEvent)event;
					logger.info("dialEvent src " + dialEvent.getSrc() + " callerChannel " + callerChannel.getDescriptor().getId());
					
					if(dialEvent.getSrc().equals(callerChannel.getDescriptor().getId())){
						logger.info("match");
						logger.info("calledChannel " + calledChannel);
						logger.info("dialEvent.getDestination() " + dialEvent.getDestination());
						
						calledChannel.getDescriptor().setId(dialEvent.getDestination());
						setState(Call.CONNECTING_STATE, "Dialing");
	    				logger.info("DialEvent CONNECTING " + dialEvent.getDestination());
						return true;
					}
				}else if(event instanceof HangupEvent){
					HangupEvent hangupEvent = (HangupEvent)event;
					if(hangupEvent.getChannel().equals(callerChannel.getDescriptor().getId())){
						setState(Call.INVALID_STATE,"No Route");
						logger.info("No Route for caller " + callerChannel.getDescriptor().getId());
						return true;
					}
				}/*else if(event instanceof NewChannelEvent){
					
					NewChannelEvent newChannelEvent = (NewChannelEvent)event;
					
					String matcher = newChannelEvent.getChannel().substring(0,newChannelEvent.getChannel().indexOf("-"));
					
					if(matcher.equals(calledChannel.getDescriptor().getId())){
						// my event set as processed;
						calledChannel.getDescriptor().setId(newChannelEvent.getChannel());
						logger.info("my destination channel " + calledChannel.getDescriptor().getId());
						return true;
					}
				}*/
				break;

			case Call.CONNECTING_STATE:
				if(event instanceof LinkEvent){
					LinkEvent linkEvent = (LinkEvent)event;
					if(linkEvent.getChannel1().equals(callerChannel.getDescriptor().getId())
							||linkEvent.getChannel2().equals(calledChannel.getDescriptor().getId()) 
							){
						setState(Call.ACTIVE_STATE,"Answered");
						logger.info("LinkEvent Answered ACTIVE " + linkEvent.getChannel2());
						return true;
					}
				}/*			
				if(event instanceof NewChannelEvent) {
	        		NewChannelEvent nce = (NewChannelEvent) event;
	        		logger.info("nce " + nce);
	        		if(nce.getChannel().equals(calledChannel.getDescriptor().getId())) {        			
	        			setState(Call.CONNECTING_STATE, "Ringing");
	        			logger.info("Ringing");
	    				return true;
	        		}
	        	} */else if(event instanceof HangupEvent) {
	        		HangupEvent he = (HangupEvent) event;
	        		if(he.getChannel().equals(callerChannel.getDescriptor().getId()) || 
	        		   he.getChannel().equals(calledChannel.getDescriptor().getId())) {
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
	    			if(le.getChannel1().equals(callerChannel.getDescriptor().getId())) {
	    				setState(Call.ACTIVE_STATE, "Answered");
	    				logger.info("Answered");
	    				return true;
	    			}	
	    		}/*				
				if(event instanceof LinkEvent){
					LinkEvent linkEvent = (LinkEvent)event;
					if(linkEvent.getChannel1().equals(callerChannel.getDescriptor().getId())
							||linkEvent.getChannel2().equals(calledChannel.getDescriptor().getId()) 
							){
						setState(Call.ACTIVE_STATE,"Answered");
						logger.info("LinkEvent Answered ACTIVE " + linkEvent.getChannel2());
						return true;
					}
				}*/				
				break;

			case Call.ACTIVE_STATE:
				if(event instanceof UnlinkEvent) {
	    			UnlinkEvent ue = (UnlinkEvent) event;
	    			if(ue.getChannel1().equals(callerChannel.getDescriptor().getId())) {
	    				setState(Call.INVALID_STATE, "Call Ended");
	    				logger.info("Call Ended");
	    				return true;
	    			}
	    		}else if(event instanceof HangupEvent){
	    			HangupEvent he = (HangupEvent) event;
	        		if(he.getChannel().equals(callerChannel.getDescriptor().getId()) || 
	        		   he.getChannel().equals(calledChannel.getDescriptor().getId())) {
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
				}
				break;
		}
		
		// not processed
		return false;

	}

	public int getState() {
		return state;
	}

//	public void setState(int state,String reasonForStateChange) {
//		stateChanged(this.getState(), this);
//		this.state = state;
//	}

	public void setState(int state) {
		this.state = state;
	}


	public Channel getCallerChannel() {
		return callerChannel;
	}

	public Channel getCalledChannel() {
		return calledChannel;
	}

	@Override
	public String toString() {
		StringBuffer st = new StringBuffer()
		.append("[caller=" + callerChannel.getDescriptor().getId() + ",called=" + calledChannel.getDescriptor().getId() +",state="+state+"]");
		return st.toString();
	}

}
