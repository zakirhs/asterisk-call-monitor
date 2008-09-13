package org.mik.asterisk.model.call;

import java.util.Iterator;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.MeetMeJoinEvent;
import org.asteriskjava.manager.event.NewChannelEvent;
import org.asteriskjava.manager.event.NewExtenEvent;
import org.asteriskjava.manager.event.NewStateEvent;
import org.mik.asterisk.model.Provider;
import org.mik.asterisk.model.call.impl.CallEndpoint;
import org.mik.asterisk.model.call.impl.Channel;
import org.mik.asterisk.model.call.impl.ConferenceCall;
import org.mik.asterisk.model.call.impl.SinglePartyCall;
import org.mik.asterisk.model.call.impl.TwoPartiesCall;
import org.mik.asterisk.model.utils.DateFormatter;

public class CallConstruction {	

	private static Logger logger = Logger.getLogger(CallConstruction.class);

	private static final int NEWEXTEN_STATE = 1;
	private static final int NEWSTATE_UP_STATE = 3;
	private static final int MEETMEJOIN_STATE = 4;	
	
	private Provider provider;
	private Stack eventStack;
	private int waitState;
	
	public CallConstruction(Provider provider, NewChannelEvent nce) {
		if(provider == null) throw new IllegalArgumentException("provider can not be null");
		if(nce == null) throw new IllegalArgumentException("nce can not be null");
		if("Ring".equals(nce.getState()) == false) throw new IllegalArgumentException("the state of the nce must be Ring");
		eventStack = new Stack();
		
		this.provider = provider;
		eventStack.push(nce);
		waitState = NEWEXTEN_STATE;
		logger.info(this);
	}
	
	public boolean process(ManagerEvent event) {
		switch(waitState) {
		case NEWEXTEN_STATE:
			if(event instanceof NewExtenEvent) {
				NewExtenEvent nee = (NewExtenEvent) event;
				NewChannelEvent nce = (NewChannelEvent) eventStack.peek();
				if(nee.getUniqueId().equals(nce.getUniqueId())) {
					if("Dial".equals(nee.getApplication())) {						
						nce = (NewChannelEvent) eventStack.pop();						
						String callId = nee.getContext() + "|" + nce.getCallerId() + "|" 
							+ nee.getExtension() + "|" + DateFormatter.format(nce.getDateReceived());
						
						Channel.Descriptor dialingChannelDesc = new Channel.Descriptor(nce.getChannel(), 
							nce.getDateReceived(), new CallEndpoint(nce.getCallerId()));
						Channel.Descriptor dialedChannelDesc = new Channel.Descriptor(
							new CallEndpoint(nee.getExtension())); 
						
						TwoPartiesCall twoPartiesCall = new TwoPartiesCall(callId, nce.getDateReceived(), 
							dialingChannelDesc, dialedChannelDesc);
											
						provider.removeCallConstruction(this);
						provider.attachCall(twoPartiesCall);
						return true;
					} else {
						eventStack.push(nee);
						waitState = NEWSTATE_UP_STATE;
						logger.info("NEWSTATE_UP_STATE");
						return true;						
					}					
				}
			} 
			break;
		case NEWSTATE_UP_STATE:
			if(event instanceof NewStateEvent) {
				NewStateEvent nse = (NewStateEvent) event;
				NewExtenEvent nee = (NewExtenEvent) eventStack.peek();
				if(nse.getUniqueId().equals(nee.getUniqueId())) {					
					if("MeetMe".equals(nee.getApplication()) || "AGI".equals(nee.getApplication())) {
						eventStack.push(nse);
						waitState = MEETMEJOIN_STATE;
						logger.info("MEETMEJOIN_STATE");
						return true;
					} else {
						nee = (NewExtenEvent) eventStack.pop();
						NewChannelEvent nce = (NewChannelEvent) eventStack.pop();
						String callId = nee.getContext() + "|" + nce.getCallerId() + "|" 
							+ nee.getExtension() + "|" + DateFormatter.format(nee.getDateReceived());
						
						Channel.Descriptor channelDesc = new Channel.Descriptor(
							nce.getChannel(), nce.getDateReceived(), new CallEndpoint(nee.getExtension())); 
						SinglePartyCall singlePartyCall = new SinglePartyCall(callId, nee.getDateReceived(),  
							Call.ACTIVE_STATE, channelDesc);
						provider.removeCallConstruction(this);
						provider.attachCall(singlePartyCall);
						return true;
					}
				}
			} else if(event instanceof HangupEvent) {
				HangupEvent he = (HangupEvent) event;
				NewExtenEvent nee = (NewExtenEvent) eventStack.peek();
				if(he.getUniqueId().equals(nee.getUniqueId())) {
					provider.removeCallConstruction(this);
				}
			}
			break;
		case MEETMEJOIN_STATE:
			if(event instanceof MeetMeJoinEvent) {
				MeetMeJoinEvent mmje = (MeetMeJoinEvent) event;
				NewStateEvent nse = (NewStateEvent) eventStack.pop();
				NewExtenEvent nee = (NewExtenEvent) eventStack.pop();
				NewChannelEvent nce = (NewChannelEvent) eventStack.pop();				
				String roomId = mmje.getMeetMe();
				boolean confCallExist = false;
				for(Iterator iter = provider.getAttachedCalls().iterator(); iter.hasNext();) {
					Call attachedCall = (Call) iter.next();
					if(attachedCall instanceof ConferenceCall) {								
						ConferenceCall confCall = (ConferenceCall) attachedCall;						
						if(confCall.getRoomId().equals(roomId)) {
							confCallExist = true;							
							Channel.Descriptor newChannelDesc = new Channel.Descriptor(nce.getChannel(), 
								nce.getDateReceived(), new CallEndpoint(nee.getExtension()));
							confCall.addChannel(newChannelDesc);							
							provider.removeCallConstruction(this);
							return true;
						}
					}
				}							
				if(!confCallExist) {					
					Channel.Descriptor channelDesc = new Channel.Descriptor(nce.getChannel(), 
						nce.getDateReceived(), new CallEndpoint(nee.getExtension()));					
					ConferenceCall confCall = new ConferenceCall(roomId, mmje.getDateReceived(), channelDesc);
					provider.removeCallConstruction(this);
					provider.attachCall(confCall);
					return true;					
				} 
			}
			break;
		}	
		
		return false;
	}
}