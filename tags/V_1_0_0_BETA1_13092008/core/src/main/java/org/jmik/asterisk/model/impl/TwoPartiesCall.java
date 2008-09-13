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
	private int state = Call.IDLE_STATE;

	public TwoPartiesCall(String callId,Date date,Channel.Descriptor dialing,Channel.Descriptor dialed){
		super(callId, date, Call.IDLE_STATE, dialed);
		callerChannel = new Channel(dialing);
		calledChannel = new Channel(dialed);
		logger.info("IDLE " + this);
	}

	public boolean process(ManagerEvent event){
		logger.info("STATE " + this.getState() + " process event " +event);
		
		switch (state) {
		
			case Call.IDLE_STATE:
				if(event instanceof DialEvent){
					DialEvent dialEvent = (DialEvent)event;
					
					if(dialEvent.getSrc().equals(callerChannel.getDescriptor().getId())){
						calledChannel.getDescriptor().setId(dialEvent.getDestination());
						setState(Call.CONNECTING_STATE,"Dialing");
						logger.info("DialEvent CONNECTING " + dialEvent.getDestination());
						return true;
					}
				}else if(event instanceof HangupEvent){
					HangupEvent hangupEvent = (HangupEvent)event;
					if(hangupEvent.getChannel().equals(callerChannel.getDescriptor().getChannel())){
						setState(Call.INVALID_STATE,"No Route");
						logger.info("No Route for caller " + callerChannel.getDescriptor().getId());
						return true;
					}
				}else if(event instanceof NewChannelEvent){
					NewChannelEvent newChannelEvent = (NewChannelEvent)event;
					
					String matcher = newChannelEvent.getChannel().substring(0,newChannelEvent.getChannel().indexOf("-"));
					
					if(matcher.equals(calledChannel.getDescriptor().getId())){
						// my event set as processed;
						calledChannel.getDescriptor().setId(newChannelEvent.getChannel());
						logger.info("my destination channel " + calledChannel.getDescriptor().getId());
						return true;
					}
				}
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
				}				
				break;

			case Call.ACTIVE_STATE:
				if(event instanceof UnlinkEvent){
					UnlinkEvent unlinkEvent = (UnlinkEvent)event;
					if(unlinkEvent.getChannel1().equals(callerChannel.getDescriptor().getId())
							||unlinkEvent.getChannel2().equals(calledChannel.getDescriptor().getId()) 
							){
						logger.info("unlinkEvent " + unlinkEvent.getChannel2());
						return true;
					}
				}else if(event instanceof HangupEvent){
					HangupEvent hangupEvent = (HangupEvent)event;
					
					if(hangupEvent.getChannel().equals(callerChannel.getDescriptor().getChannel()) || 
							hangupEvent.getChannel().equals(calledChannel.getDescriptor().getChannel())){
//							if(hangupEvent.getCause().intValue() == 0){
							setState(Call.INVALID_STATE,"Hangup");
							logger.info("HangupEvent INVALID Hangup " + hangupEvent.getChannel()+ " cause " + hangupEvent.getCauseTxt());
//							}
							return true;
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

	public void setState(int state,String reasonForStateChange) {
		stateChanged(this.getState(), this);
		this.state = state;
	}

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
		.append("[caller=" + callerChannel.getDescriptor().getId() + ",called=" + calledChannel.getDescriptor().getExtension() +",state="+state+"]");
		return st.toString();
	}

}
