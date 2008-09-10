package org.jmik.asterisk.model.impl;

import java.util.Date;

import org.apache.log4j.Logger;
import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.ManagerEvent;

/**
 * This class represents a call with only one participant (thus one channel).
 * It happens when someone call an extension which is configured to switch
 * the call to an Asterisk's build-in IVR.
 * 
 * @author Michele La Porta
 */
public class SinglePartyCall extends Call{

	private static Logger logger = Logger.getLogger(SinglePartyCall.class);

	public SinglePartyCall(String callId,Date date,int callState,Channel.Descriptor channelDescriptor){
		super(callId, date, callState, channelDescriptor);
		state = Call.ACTIVE_STATE;
		setState(Call.ACTIVE_STATE, "Active");
		logger.info("ACTIVED " + this);// current Listeners " + this.getListeners());
	}

	protected void setState(int state,String reasonForStateChange){
		stateChanged(state, this);
		this.state = state;
	}

	public boolean process(ManagerEvent event){
		switch (state) {
			case Call.ACTIVE_STATE:
				if(event instanceof HangupEvent){
					HangupEvent hangupEvent = (HangupEvent)event;
					if(hangupEvent.getChannel().equals(channel.getDescriptor().getId())){
						logger.info("hangupEvent " + hangupEvent);
						setState(Call.INVALID_STATE,"Hangup");
						return true;
					}
				}
				break;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuffer st = new StringBuffer()
		.append(this.getClass().getSimpleName()+"@"+this.hashCode()+ "[channel="+channel.getDescriptor().getId() +",state="+state+"]");
		return st.toString();
	}

}
