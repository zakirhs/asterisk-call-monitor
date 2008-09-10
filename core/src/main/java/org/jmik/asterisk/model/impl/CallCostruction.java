package org.jmik.asterisk.model.impl;

import java.util.Stack;

import org.apache.log4j.Logger;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.MeetMeJoinEvent;
import org.asteriskjava.manager.event.NewChannelEvent;
import org.asteriskjava.manager.event.NewExtenEvent;
import org.asteriskjava.manager.event.NewStateEvent;
import org.jmik.asterisk.model.Provider;
import org.jmik.asterisk.model.utils.DateFormatter;

/**
 * FSM that keeps track of the events.
 * instantiate Call
 * start --> wait:NEW_EXT 
 * start > NEW_EXT > NewExtenEvent > state==DIAL : TwoPartiesCall ? NEWSTATE_UP 
 * > NewStateEvent > 
 * application=AGI/MEETME ? MEETMEJOIN > MeetMeJoinEvent > ConferenceCall
 * 
 * @author michele
 */
public class CallCostruction {

	private static Logger logger = Logger.getLogger(CallCostruction.class);

	public static final int WAIT_NEW_EXT = 0;
	public static final int WAIT_NEW_STATE_UP = 1;
	public static final int WAIT_MEETMEJOIN = 2;

	private int waitState = -1;
	private Provider provider;
	public Stack<ManagerEvent> eventStack = new Stack<ManagerEvent>();
	
	public CallCostruction(Provider provider,NewChannelEvent newChannelEvent){
		this.provider = provider;
		waitState = WAIT_NEW_EXT;
		eventStack.add(newChannelEvent);
	}

	public boolean processEvent(ManagerEvent event){
		
//		logger.info("processEvent " + event.getClass().getSimpleName()+" " + waitState);
		
		switch (waitState) 
		{
		
			case WAIT_NEW_EXT:
				
				if(event instanceof NewExtenEvent){

					NewExtenEvent newExtenEvent = (NewExtenEvent)event;
					NewChannelEvent newChannelEvent = (NewChannelEvent)eventStack.peek();
					
					if(newExtenEvent.getUniqueId().equals(newChannelEvent.getUniqueId())){

						if(newExtenEvent.getApplication().equals("Dial")){
							
							newChannelEvent = (NewChannelEvent)eventStack.pop();
							
							//default|johndoe|666|Sat Sep 06 17:48:35 CEST 2008
							String callId = newExtenEvent.getContext()
								+ "|" + newChannelEvent.getCallerId()
								+ "|" + newExtenEvent.getExtension()
								+ "|" + DateFormatter.format(newChannelEvent
										.getDateReceived());
							
							logger.info("callId:" + callId);
							
							Channel.Descriptor callerChannelDescriptor = new Channel.Descriptor(
								newChannelEvent.getChannel(), newChannelEvent
										.getDateReceived(), new CallEndpoint(newExtenEvent.getExtension()));
							
							logger.info("caller:" + callerChannelDescriptor);
							
							Channel.Descriptor calledChannelDescriptor = new Channel.Descriptor(
								newExtenEvent.getAppData(), newExtenEvent
										.getDateReceived(), new CallEndpoint(
										newExtenEvent.getExtension()));
							
							logger.info("called:" + calledChannelDescriptor.getEndpoint().getCallId()+ " dialed:" + newExtenEvent.getAppData());
							
							//instantiate a new two parties call 
							TwoPartiesCall twoPartiesCall = new TwoPartiesCall(callId,newChannelEvent.getDateReceived(),callerChannelDescriptor,calledChannelDescriptor);
							provider.attachCall(twoPartiesCall);
							provider.removeCallConstruction(this);
							logger.info("instantiate a new twoPartiesCall " + twoPartiesCall);
							return true;
							
						}else if(newExtenEvent.getApplication().equals("Playback") 
								||newExtenEvent.getApplication().equals("MeetMe")
								||newExtenEvent.getApplication().equals("AGI")
								){
							waitState = WAIT_NEW_STATE_UP;
							eventStack.push(newExtenEvent);
							logger.info("WAIT_NEW_STATE_UP " + this);
							return true;
						}
					}
				}
				
				break;
				
			case WAIT_NEW_STATE_UP:
				
				if(event instanceof NewStateEvent){
					
					NewStateEvent newStateEvent = (NewStateEvent)event;
					NewExtenEvent newExtenEvent = (NewExtenEvent)eventStack.peek();
					
					if(newStateEvent.getUniqueId().equals(newExtenEvent.getUniqueId())){
						
						if(newExtenEvent.getApplication().equals("Playback") ){

							newExtenEvent = (NewExtenEvent)eventStack.pop();
							NewChannelEvent newChannelEvent = (NewChannelEvent)eventStack.pop();

							String callId = newExtenEvent.getContext()
								+ "|"
								+ newChannelEvent.getCallerId()
								+ "|"
								+ newExtenEvent.getExtension()
								+ "|"
								+ DateFormatter.format(newChannelEvent
										.getDateReceived());

							Channel.Descriptor channelDescriptor = new Channel.Descriptor(newChannelEvent.getChannel(), newChannelEvent.getDateReceived(), new CallEndpoint(newChannelEvent.getCallerId()));
							
							//instantiate a new single party call 
							SinglePartyCall singleCall = new SinglePartyCall(callId,newChannelEvent.getDateReceived(),CallState.ACTIVE.state(),channelDescriptor);
							provider.removeCallConstruction(this);
							provider.attachCall(singleCall);
							logger.info("instantiate a new singleCall " + singleCall);
							
						}else if(newExtenEvent.getApplication().equals("MeetMe")){
							waitState = WAIT_MEETMEJOIN;
							logger.info("WAIT_MEETMEJOIN " + this);
							eventStack.push(newStateEvent);
						}
						/*else if(newExtenEvent.getApplication().equals("AGI")){
							//TODO
							waitState = WAIT_AGI;
							eventStack.push(newExtenEvent);
						}*/
					}
				}
				
				break;
				
			case WAIT_MEETMEJOIN:
				
				if(event instanceof MeetMeJoinEvent){
					
					MeetMeJoinEvent meetMeJoinEvent = (MeetMeJoinEvent)event;
					String roomId = meetMeJoinEvent.getMeetMe();
					logger.info("roomId " + roomId);
					
					NewStateEvent newStateEvent = (NewStateEvent)eventStack.pop();
					NewExtenEvent newExtenEvent = (NewExtenEvent)eventStack.pop();
					NewChannelEvent newChannelEvent = (NewChannelEvent)eventStack.pop();
					
					boolean conferenceCallExist = false;
					logger.info("attachedCalls size " + provider.getAttachedCalls().size());
					
					for(Call call :provider.getAttachedCalls()){
						
						if(call instanceof ConferenceCall){
							ConferenceCall conferenceCall = (ConferenceCall)call;
							logger.info("processing conferenceCall roomId " + conferenceCall.getId() + " channels size" + conferenceCall.getChannels().size()+ " " + conferenceCall.getChannels());
							
							if(conferenceCall.getId().equals(roomId)){
								
								conferenceCallExist = true;
								
								Channel.Descriptor newChannelDescriptor = new Channel.Descriptor(newChannelEvent.getChannel(),newChannelEvent.getDateReceived(),new CallEndpoint(newExtenEvent.getExtension()));
								conferenceCall.addChannel(newChannelDescriptor);
								logger.info("added Channel " + newChannelDescriptor.getChannel()+ " to roomId " + conferenceCall.getId());
								
								provider.removeCallConstruction(this);
								logger.info("Conference " + conferenceCall.getId() + " Exists channels[" + conferenceCall.getChannels().size() +"]");
								return true;
							}
						}
					}
					
					if(!conferenceCallExist){
						//instantiate a new conference call
						Channel.Descriptor newChannelDescriptor = new Channel.Descriptor(
							newChannelEvent.getChannel(), newChannelEvent
									.getDateReceived(), new CallEndpoint(
									newExtenEvent.getExtension()));
						
						ConferenceCall conferenceCall = new ConferenceCall(roomId,
							meetMeJoinEvent.getDateReceived(),
							newChannelDescriptor);
						
						provider.removeCallConstruction(this);
						provider.attachCall(conferenceCall);
						logger.info("instantiate a new conferenceCall " + conferenceCall);
						return true;
					}
				}
				break;
		}
		
		return false;
	}
	
	/**
	public boolean processEvent(ManagerEvent event){
		logger.info("processEvent " + event.getClass().getSimpleName()+" " + waitState);
		switch (waitState) {
		
		case WAIT_NEW_EXT:
			
			if(event instanceof NewExtenEvent){
				
				NewExtenEvent newExtenEvent = (NewExtenEvent)event;
				NewChannelEvent newChannelEvent = (NewChannelEvent)eventStack.peek();
				
				// match living contructions channeChannel.Descriptor channelDescriptor = new Channel.Descriptor(newChannelEvent.getChannel(), newChannelEvent.getDateReceived(), new CallEndpoint(newChannelEvent.getCallerId()));
				
//				newChannelEvent = (NewChannelEvent)eventStack.pop();
//				
//				String callId = newExtenEvent.getContext() + "|" + newChannelEvent.getCallerId() + "|" + newExtenEvent.getExtension() + "|" + DateFormatter.format(newChannelEvent.getDateReceived());
//				Channel.Descriptor channelDescriptor = new Channel.Descriptor(newChannelEvent.getChannel(), newChannelEvent.getDateReceived(), new CallEndpoint(newChannelEvent.getCallerId()));
				
				//instantiate a new single party call 
//				SinglePartyCall singleCall = new SinglePartyCall(callId,newChannelEvent.getDateReceived(),CallState.ACTIVE.state(),channelDescriptor);
//				provider.removeCallConstruction(this);
//				provider.attachCall(singleCall);
//				logger.info("instantiate a new singleCall " + singleCall);l?
				if(newExtenEvent.getUniqueId().equals(newChannelEvent.getUniqueId())){
					
					if(newExtenEvent.getApplication().equals("Playback")){
						eventStack.push(newExtenEvent);
						waitState = WAIT_NEW_STATE_UP;
						
//						Channel.Descriptor channelDescriptor = new Channel.Descriptor(newChannelEvent.getChannel(), newChannelEvent.getDateReceived(), new CallEndpoint(newChannelEvent.getCallerId()));
						
//						newChannelEvent = (NewChannelEvent)eventStack.pop();
//						
//						String callId = newExtenEvent.getContext() + "|" + newChannelEvent.getCallerId() + "|" + newExtenEvent.getExtension() + "|" + DateFormatter.format(newChannelEvent.getDateReceived());
//						Channel.Descriptor channelDescriptor = new Channel.Descriptor(newChannelEvent.getChannel(), newChannelEvent.getDateReceived(), new CallEndpoint(newChannelEvent.getCallerId()));
						
						//instantiate a new single party call 
//						SinglePartyCall singleCall = new SinglePartyCall(callId,newChannelEvent.getDateReceived(),CallState.ACTIVE.state(),channelDescriptor);
//						provider.removeCallConstruction(this);
//						provider.attachCall(singleCall);
//						logger.info("instantiate a new singleCall " + singleCall);
						return true;
						
					}else if(newExtenEvent.getApplication().equals("Dial")){
						
						newChannelEvent = (NewChannelEvent)eventStack.pop();
						String callId = newExtenEvent.getContext() + "|" + newChannelEvent.getCallerId() + "|" + newExtenEvent.getExtension() + "|" + DateFormatter.format(newChannelEvent.getDateReceived());
						
						Channel.Descriptor dialingChannelDescriptor = new Channel.Descriptor(newChannelEvent.getChannel(), newChannelEvent.getDateReceived(), new CallEndpoint(newChannelEvent.getCallerId()));
						logger.info("dialing " + dialingChannelDescriptor);
						Channel.Descriptor dialedChannelDescriptor = new Channel.Descriptor(newExtenEvent.getExtension());//,newExtenEvent.getDateReceived(), new CallEndpoint(newExtenEvent.getCallerId());
						logger.info("dialed " + newExtenEvent.getExtension());
						
						//instantiate a new two parties call 
						TwoPartiesCall twoPartiesCall = new TwoPartiesCall(callId,newChannelEvent.getDateReceived(),dialingChannelDescriptor,dialedChannelDescriptor);
						provider.removeCallConstruction(this);
						provider.attachCall(twoPartiesCall);
						logger.info("instantiate a new twoPartiesCall " + twoPartiesCall);
						return true;
						
					}else if(newExtenEvent.getApplication().equals("MeetMe")){
						waitState = WAIT_NEW_STATE_UP;
//						newChannelEvent = (NewChannelEvent)eventStack.pop();
						
//						String callId = newExtenEvent.getContext() + "|" + newChannelEvent.getCallerId() + "|" + newExtenEvent.getExtension() + "|" + DateFormatter.format(newChannelEvent.getDateReceived());
//						Channel.Descriptor channelDescriptor = new Channel.Descriptor(newChannelEvent.getChannel(), newChannelEvent.getDateReceived(), new CallEndpoint(newChannelEvent.getCallerId()));
						
						//instantiate a new conference call
//						ConferenceCall conferenceCall = new ConferenceCall(callId,newChannelEvent.getDateReceived(),channelDescriptor);
//						provider.removeCallConstruction(this);
//						provider.attachCall(conferenceCall);
//						logger.info("instantiate a new conferenceCall " + conferenceCall);
//					break;	
						return true;
						
					}else if(newExtenEvent.getApplication().equals("AGI")){
						//TODO
					}

				}
				
			}else {
				logger.info("" + "event[" + event.getClass().getSimpleName()+"]");
				if(event instanceof HangupEvent){
					HangupEvent hangupEvent = (HangupEvent)event;
					NewChannelEvent newChannelEvent = (NewChannelEvent)eventStack.peek();
					logger.info("" + "event[" + event.getClass().getSimpleName()+"] " +hangupEvent);
					logger.info("" + "event[" + newChannelEvent.getClass().getSimpleName()+"] " +newChannelEvent);
					
					if(hangupEvent.getUniqueId().endsWith(newChannelEvent.getUniqueId())){
						provider.removeCallConstruction(this);
						logger.info("removeCallConstruction this ");
												
					}
				}
			}
			
			break;
/*
		case NEWSTATE_UP_STATE:
			logger.info("NEWSTATE_UP_STATE"+" event[" + event.getClass().getSimpleName() +"]");
			
			if(event instanceof NewStateEvent){
				
				NewStateEvent newStateEvent = (NewStateEvent)event;
//				logger.info("process NewStateEvent " + newStateEvent);
				
				NewExtenEvent newExtenEvent = (NewExtenEvent)eventStack.peek();
//				logger.info("processEvent peek " + newExtenEvent);
				logger.info("newStateEvent.getUniqueId() " + newStateEvent.getUniqueId());
				logger.info("newExtenEvent.getUniqueId() " + newExtenEvent.getUniqueId());
				
				if(newStateEvent.getUniqueId().equals(newExtenEvent.getUniqueId())){
					
					if(newExtenEvent.getApplication().equals("MeetMe") || newExtenEvent.getApplication().equals("AGI") ){
						eventStack.push(newStateEvent);//add
						waitState = MEETMEJOIN_STATE;
						logger.info("processEvent Meetme/AGI push " + newStateEvent +" waitState " +waitState);
						return true;
					}else{
						newExtenEvent = (NewExtenEvent) eventStack.pop();
						logger.info("processEvent pop " + newExtenEvent);
						
						NewChannelEvent newChannelEvent = (NewChannelEvent)eventStack.pop();
						logger.info("processEvent pop " + newChannelEvent);

						String callId = newExtenEvent.getContext()+ "|"+ newChannelEvent.getCallerId()+ "|"+ newExtenEvent.getExtension()+ "|"+ DateFormatter.format(newChannelEvent.getDateReceived());
						logger.info("processEvent newStateEvent callId" + callId);
						
						Channel.Descriptor channelDescriptor = new Channel.Descriptor(newChannelEvent.getChannel(), newChannelEvent.getDateReceived(), new CallEndpoint(newChannelEvent.getCallerId()));
						SinglePartyCall singlePartyCall = new SinglePartyCall(callId,newChannelEvent.getDateReceived(),Call.ACTIVE_STATE,channelDescriptor);
						provider.removeCallConstruction(this);
						provider.attachCall(singlePartyCall);
						return true;
					}
				}
				
			}else if (event instanceof HangupEvent){
				HangupEvent hangupEvent = (HangupEvent)event;
				logger.info("processEvent hangupEvent " + hangupEvent);
				
				NewExtenEvent newExtenEvent = (NewExtenEvent)eventStack.peek();
				if(hangupEvent.getUniqueId().equals(newExtenEvent.getUniqueId())){
					provider.removeCallConstruction(this);
					logger.info("processEvent remove this CallConstruction ");
				}
			}
			
			break;

		case MEETMEJOIN_STATE:
			logger.info("MEETMEJOIN_STATE"+" event[" + event.getClass().getSimpleName() +"]");
			
			if(event instanceof MeetMeJoinEvent){
				MeetMeJoinEvent meetMeJoinEvent = (MeetMeJoinEvent)event;
				logger.info("MeetMeJoinEvent " + meetMeJoinEvent);
				NewStateEvent newStateEvent = (NewStateEvent)eventStack.pop();
				NewExtenEvent newExtenEvent = (NewExtenEvent)eventStack.pop();
				NewChannelEvent newChannelEvent = (NewChannelEvent)eventStack.pop();
				String roomId = meetMeJoinEvent.getMeetMe();
				boolean conferenceCallExist = false;
				
				for(Iterator iter = provider.getAttachedCalls().iterator();iter.hasNext();){
					Call attachedCall = (Call)iter.next();
					if(attachedCall instanceof ConferenceCall){
						ConferenceCall conferenceCall = (ConferenceCall)attachedCall;
						if(conferenceCall.getId().equals(roomId)){
							logger.info("roomId " + roomId + " exists");
							
							conferenceCallExist = true;
							Channel.Descriptor channelDescriptor = new Channel.Descriptor(newChannelEvent.getChannel(),newChannelEvent.getDateReceived(),new CallEndpoint(newExtenEvent.getExtension()));
							conferenceCall.addChannel(channelDescriptor);
							provider.removeCallConstruction(this);
							logger.info("remove this CallConstruction " + meetMeJoinEvent);
							
							return true;
						}
					}
				}
				if(!conferenceCallExist){
					Channel.Descriptor channelDescriptor = new Channel.Descriptor(newChannelEvent.getChannel(),newChannelEvent.getDateReceived(),new CallEndpoint(newExtenEvent.getExtension()));
					ConferenceCall conferenceCall = new ConferenceCall(roomId,meetMeJoinEvent.getDateReceived(),channelDescriptor);
					provider.removeCallConstruction(this);
					provider.attachCall(conferenceCall);
					return true;
				}
				
			}else {
				logger.info("! MeetMeJoinEvent state " + waitState+" event[" + event.getClass().getSimpleName() +"]");
				break;
			}
			
		default:
			throw new RuntimeException("unknown state");
		}
		
		return true;
		
	}*/

	public int getWaitState() {
		return waitState;
	}

	public Stack<ManagerEvent> getEventStack() {
		return eventStack;
	}
	
}
