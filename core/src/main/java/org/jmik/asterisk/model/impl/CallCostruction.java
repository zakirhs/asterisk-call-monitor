package org.jmik.asterisk.model.impl;

import java.util.Iterator;
import java.util.Stack;

import org.apache.log4j.Logger;
import org.asteriskjava.manager.event.HangupEvent;
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

	public static final int WAIT_NEW_EXT = 1;
	public static final int WAIT_NEW_STATE_UP = 3;
	public static final int WAIT_MEETMEJOIN = 4;

	private int waitState;
	private Provider provider;
	public Stack<ManagerEvent> eventStack = new Stack<ManagerEvent>();
	
	public CallCostruction(Provider provider,NewChannelEvent newChannelEvent){
		if(provider == null){
			logger.error("provider " + provider);
			throw new IllegalArgumentException("provider can not be null");
		}
		if(newChannelEvent == null){
			logger.error("newChannelEvent " + newChannelEvent);
			throw new IllegalArgumentException("newChannelEvent can not be null");
		}
		if("Down".equals(newChannelEvent.getState()) == false){
			logger.error("newChannelEvent Ring? " + newChannelEvent.getState());
			throw new IllegalArgumentException("the state of the nce must be Ring");
		}
		
		this.provider = provider;
		waitState = WAIT_NEW_EXT;
		eventStack.push(newChannelEvent);
		logger.info(this + " " + newChannelEvent);
		
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
						logger.info("match " +("Dial".equals(newExtenEvent.getApplication())));
						
						if(newExtenEvent.getApplication().equals("Dial")) {						
							newChannelEvent = (NewChannelEvent) eventStack.pop();						
							logger.info("popped newChannelEvent " + newChannelEvent);
							
							String callId = newExtenEvent.getContext() + "|" + newChannelEvent.getCallerId() + "|" 
								+ newExtenEvent.getExtension() + "|" + DateFormatter.format(newChannelEvent.getDateReceived());
							logger.info("popped newChannelEvent callId " + callId);
							
							Channel.Descriptor dialingChannelDesc = new Channel.Descriptor(newChannelEvent.getChannel(), 
									newChannelEvent.getDateReceived(), new CallEndpoint(newChannelEvent.getCallerId()));
							logger.info("dialingChannelDesc " + dialingChannelDesc);
							
							Channel.Descriptor dialedChannelDesc = new Channel.Descriptor(
								new CallEndpoint(newExtenEvent.getExtension())); 
							logger.info("dialedChannelDesc " + dialedChannelDesc);
							logger.info("dialedChannelDesc Extension " + newExtenEvent.getExtension());
							
							TwoPartiesCall twoPartiesCall = new TwoPartiesCall(callId, newChannelEvent.getDateReceived(), 
								dialingChannelDesc, dialedChannelDesc);
							logger.info("twoPartiesCall " + twoPartiesCall);
												
							provider.removeCallConstruction(this);
							logger.info("twoPartiesCall removeCallConstruction " +this);
							provider.attachCall(twoPartiesCall);
							logger.info("attachCall " +twoPartiesCall);
							return true;
						} else {
							eventStack.push(newExtenEvent);
							waitState = WAIT_NEW_STATE_UP;
							logger.info("WAIT_NEW_STATE_UP");
							return true;						
						}
//						if(newExtenEvent.getApplication().equals("Dial")){
//							
//							newChannelEvent = (NewChannelEvent)eventStack.pop();
//							
//							//default|johndoe|666|Sat Sep 06 17:48:35 CEST 2008
//							String callId = newExtenEvent.getContext()
//								+ "|" + newChannelEvent.getCallerId()
//								+ "|" + newExtenEvent.getExtension()
//								+ "|" + DateFormatter.format(newChannelEvent
//										.getDateReceived());
//							
//							logger.info("callId:" + callId);
//							
//							Channel.Descriptor callerChannelDescriptor = new Channel.Descriptor(
//								newChannelEvent.getChannel(), newChannelEvent
//										.getDateReceived(), new CallEndpoint(newExtenEvent.getExtension()));
//							
//							logger.info("caller:" + callerChannelDescriptor);
//							
//							Channel.Descriptor calledChannelDescriptor = new Channel.Descriptor(
//								newExtenEvent.getAppData(), newExtenEvent
//										.getDateReceived(), new CallEndpoint(
//										newExtenEvent.getExtension()));
//							
//							logger.info("called:" + calledChannelDescriptor.getEndpoint().getId()+ " dialed:" + newExtenEvent.getAppData());
//							
//							//instantiate a new two parties call 
//							TwoPartiesCall twoPartiesCall = new TwoPartiesCall(callId,newChannelEvent.getDateReceived(),callerChannelDescriptor,calledChannelDescriptor);
//							provider.attachCall(twoPartiesCall);
//							provider.removeCallConstruction(this);
//							logger.info("instantiate a new twoPartiesCall " + twoPartiesCall);
//							return true;
//							
//						}else /*if(newExtenEvent.getApplication().equals("Playback") 
//								||newExtenEvent.getApplication().equals("MeetMe")
//								||newExtenEvent.getApplication().equals("AGI")
//								)*/{
//							waitState = WAIT_NEW_STATE_UP;
//							eventStack.push(newExtenEvent);
//							logger.info("WAIT_NEW_STATE_UP " + this);
//							return true;
//						}
					}
				}else if(event instanceof HangupEvent) {
					HangupEvent hangupEvent = (HangupEvent) event;
					logger.info("-> HangupEvent " +hangupEvent);
					
					logger.info("eventStack " +eventStack.size());
					NewChannelEvent newChannelEvent = (NewChannelEvent) eventStack.peek();
					
					logger.info("peek NewChannelEvent " + newChannelEvent);
					
					if(hangupEvent.getUniqueId().equals(newChannelEvent.getUniqueId())) {
						provider.removeCallConstruction(this);
						logger.info("HangupEvent removeCallConstruction " +this);
					}
				}
				break;
				
			case WAIT_NEW_STATE_UP:
				
				if(event instanceof NewStateEvent){
					
					NewStateEvent newStateEvent = (NewStateEvent)event;
					NewExtenEvent newExtenEvent = (NewExtenEvent)eventStack.peek();
					
					if(newStateEvent.getUniqueId().equals(newExtenEvent.getUniqueId())){
						
						if(newExtenEvent.getApplication().equals("MeetMe")){
							waitState = WAIT_MEETMEJOIN;
							eventStack.push(newStateEvent);
							logger.info("WAIT_MEETMEJOIN  eventStack " + eventStack);
							
						}
						else/*(newExtenEvent.getApplication().equals("Playback") )*/{

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
							logger.info("singleCall removeCallConstruction " + this);
							provider.attachCall(singleCall);
							logger.info("instantiate a new singleCall " + singleCall);
							
						}/*else if(newExtenEvent.getApplication().equals("MeetMe")){
							waitState = WAIT_MEETMEJOIN;
							logger.info("WAIT_MEETMEJOIN " + this);
							eventStack.push(newStateEvent);
						}*/
						/*else if(newExtenEvent.getApplication().equals("AGI")){
							//TODO
							waitState = WAIT_AGI;
							eventStack.push(newExtenEvent);
						}*/
					}
				}else if(event instanceof HangupEvent) {
					HangupEvent he = (HangupEvent) event;
					NewExtenEvent newExtenEvent = (NewExtenEvent) eventStack.peek();
					if(he.getUniqueId().equals(newExtenEvent.getUniqueId())) {
						provider.removeCallConstruction(this);
						logger.info("HangupEvent removeCallConstruction " + this);
						
					}
				}
				
				break;
				
			case WAIT_MEETMEJOIN:
				if(event instanceof MeetMeJoinEvent) {
					MeetMeJoinEvent mmje = (MeetMeJoinEvent) event;
					NewStateEvent nse = (NewStateEvent) eventStack.pop();
					NewExtenEvent nee = (NewExtenEvent) eventStack.pop();
					NewChannelEvent nce = (NewChannelEvent) eventStack.pop();				
					String roomId = mmje.getMeetMe();
					logger.info("Processing MeetMeJoinEvent " + mmje.getMeetMe());
					
					boolean confCallExist = false;
					
					logger.info("provider.getAttachedCalls() size " + provider.getAttachedCalls().size());
					
					for(Iterator iter = provider.getAttachedCalls().iterator(); iter.hasNext();) {
						Call attachedCall = (Call) iter.next();
						if(attachedCall instanceof ConferenceCall) {								
							ConferenceCall confCall = (ConferenceCall) attachedCall;						
							logger.info("existing confCall " + confCall);
							
							if(confCall.getRoomId().equals(roomId)) {
								confCallExist = true;							
								Channel.Descriptor newChannelDescritor = new Channel.Descriptor(nce.getChannel(), 
									nce.getDateReceived(), new CallEndpoint(nee.getExtension()));
								logger.info(newChannelDescritor);
								confCall.addChannel(newChannelDescritor);							
								provider.removeCallConstruction(this);
								logger.info("confCallExist removeCallConstruction " + this);
								return true;
							}
						}
					}	
					
					if(!confCallExist) {
						
//						logger.info("nce.getChannel() " + nce.getChannel() );
//						logger.info("nee.getExtension() " + nee.getExtension() );
						
						Channel.Descriptor channelDesc = new Channel.Descriptor(nce.getChannel(), 
							nce.getDateReceived(), new CallEndpoint(nee.getExtension()));
//						logger.info("channelDesc " + channelDesc );
//						logger.info("channelDesc " + channelDesc.getId() );
//						logger.info("channelDesc " + channelDesc.getEndpoint() );
//						logger.info("channelDesc " + channelDesc.getEndpoint().getId() );
//						logger.info("channelDesc " + channelDesc.getCreationTime());
//						
//						logger.info("mmje.getDateReceived() " + mmje.getDateReceived() );
						
						ConferenceCall conferenceCall = new ConferenceCall(roomId, mmje.getDateReceived(), channelDesc);
						provider.removeCallConstruction(this);
//						logger.info("ConferenceCall removeCallConstruction " + this);
						logger.info("ConferenceCall attaching " + conferenceCall);
						provider.attachCall(conferenceCall);
						return true;					
					} 
				}				
				/*if(event instanceof MeetMeJoinEvent){
					
					MeetMeJoinEvent meetMeJoinEvent = (MeetMeJoinEvent)event;
					String roomId = meetMeJoinEvent.getMeetMe();
					logger.info("roomId " + roomId);
					
					NewStateEvent newStateEvent = (NewStateEvent)eventStack.pop();
					NewExtenEvent newExtenEvent = (NewExtenEvent)eventStack.pop();
					NewChannelEvent newChannelEvent = (NewChannelEvent)eventStack.pop();
					
					boolean conferenceCallExist = false;
					logger.info("provider attachedCalls size " + provider.getAttachedCalls().size());
					
					for(Call call :provider.getAttachedCalls()){
						
						if(call instanceof ConferenceCall){
							ConferenceCall conferenceCall = (ConferenceCall)call;
							logger.info("processing conferenceCall roomId " + conferenceCall.getId() + " channels size" + conferenceCall.getChannels().size()+ " " + conferenceCall.getChannels());
							
							if(conferenceCall.getId().equals(roomId)){
								
								conferenceCallExist = true;
								
								Channel.Descriptor newChannelDescriptor = new Channel.Descriptor(newChannelEvent.getChannel(),newChannelEvent.getDateReceived(),new CallEndpoint(newExtenEvent.getExtension()));
								conferenceCall.addChannel(newChannelDescriptor);
								logger.info("added Channel " + newChannelDescriptor.getId()+ " to roomId " + conferenceCall.getId());
								
								provider.removeCallConstruction(this);
								provider.attachCall(conferenceCall);
								
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
				}*/
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
