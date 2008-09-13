package org.jmik.asterisk.model.impl;

import java.util.Date;
import java.util.HashSet;

import org.apache.log4j.Logger;
import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.MeetMeJoinEvent;
import org.asteriskjava.manager.event.MeetMeLeaveEvent;
import org.jmik.asterisk.model.CallListener;
import org.jmik.asterisk.model.impl.Channel.Descriptor;

/**
 * This class represents a call with more than two participant (thus more than two channels) 
 * In Asterisk model conference it's more like a collection of single party calls
 * which are destinated to an extension which is configured to switch the call
 * to a particular conference room,where the information from the callers are mixed
 * and then ditributed to every participants.
 * In this JTAPI model a Call has a set of instances of Connection therefore
 * in our model a ConferenceCall is modeled to have a set of instances of Channel.
 * 
 * @author Michele La Porta
 *
 */

public class ConferenceCall extends Call {
	
	private static Logger logger = Logger.getLogger(ConferenceCall.class);

	private java.util.Set<Channel> channels;
//	private java.util.Set<Descriptor> channelDescriptors;

	public ConferenceCall(String callId, Date date,Descriptor dialed) {
		super(callId, date, Call.IDLE_STATE, dialed);
		this.channels = new HashSet<Channel>();
		channels.add(channel);
		logger.info("IDLE dialed channel:" + dialed.getChannel() +" EndpointID:" + dialed.getEndpoint().getCallId() );
	}

	public java.util.Set<Channel> getChannels() {
		return this.channels;
	}

	public void addChannel(Descriptor channelDescriptor){
//		this.channelDescriptors.add(channelDescriptor);
		logger.info("addChannel channels size " + channels.size());

		Channel channel = new Channel(channelDescriptor); 
		if(!channels.contains(channel)){
			channels.add(channel);
			logger.info("addChannel " + channelDescriptor.getChannel() + " to roomId " + channelDescriptor.getEndpoint().getCallId() +" channels " + channels);
			logger.info("addChannel channels size " + channels.size());
		}

		for(CallListener callListener : listeners){
			callListener.channelAdded(this,channel);
			logger.debug("addChannel notify call listener <"+callListener+"> of state changed");
		}
	}

	public void removeChannel(Descriptor channelDescriptor){
		logger.info("removeChannel channels size " + channels.size());
//		this.channelDescriptors.remove(channelDescriptor);
		Channel channel = new Channel(channelDescriptor);
		if(channels.contains(channel)){
			channels.remove(channel);
			logger.info("removeChannel " + channelDescriptor.getChannel() +" channels " + channels.size());
			logger.info("removeChannel channels size " + channels.size());
		}
		
		for(CallListener callListener : listeners){
			callListener.channelRemoved(this,channel);
			logger.debug("removeChannel notify call listener <"+callListener+"> of state changed");
		}
	}

	@Override
	public boolean process(ManagerEvent event) {
		
		switch (state) {
		
		case Call.IDLE_STATE:
			
			logger.debug("process " + event);
			if(event instanceof MeetMeJoinEvent){
				MeetMeJoinEvent meetMeJoinEvent = (MeetMeJoinEvent)event;
//				// new one?no will be added by call construction if conference exists
//				Channel.Descriptor descriptor = new Channel.Descriptor(meetMeJoinEvent.getChannel(),meetMeJoinEvent.getDateReceived(),new CallEndpoint("" + meetMeJoinEvent.getUserNum()));
//				this.addChannel(descriptor);
//				logger.info("IDLE MeetMeJoinEvent channels size " + channels.size());
				
//				logger.info("meetMeJoinEvent add " + descriptor.getChannel() +" channels " + channels);
				if(meetMeJoinEvent.getMeetMe().equals(this.getId())){
					setState(Call.ACTIVE_STATE, "MeetMeJoinEvent");
					logger.info("meetMeJoinEvent ACTIVE_STATE " + channels.size());
					return false;
				}
				
			}/*else if(event instanceof NewChannelEvent){
				NewChannelEvent newChannelEvent = (NewChannelEvent)event;
				logger.info("newChannelEvent " + newChannelEvent.getChannel());
//				String matcher = newChannelEvent.getChannel().substring(0,newChannelEvent.getChannel().indexOf("-"));
//				
//				if(matcher.equals(calledChannel.getDescriptor().getId())){
//					// my event set as processed;
//					calledChannel.getDescriptor().setId(newChannelEvent.getChannel());
//					logger.info("my destination channel " + calledChannel.getDescriptor().getId());
//					return true;
//				}
			}*/

		case Call.ACTIVE_STATE:

			if(event instanceof MeetMeLeaveEvent){
				
				MeetMeLeaveEvent meetMeLeaveEvent = (MeetMeLeaveEvent)event;
				
				// iterate over descriptors to find a matching channel
				for(Channel channel : channels){
					
					Channel.Descriptor descriptor = channel.getDescriptor();
					logger.info("found descriptor " + descriptor.getChannel() + " check meetMeLeaveEvent[channel=" + meetMeLeaveEvent.getChannel() + "]");
					
					if(meetMeLeaveEvent.getChannel().equals(descriptor.getChannel())){
							this.removeChannel(descriptor);
							if(channels.size() == 0)
								setState(Call.INVALID_STATE, "Conference is empty");
							return true;
					}
				}
				
				
			}else if(event instanceof HangupEvent){
				
				HangupEvent hangupEvent = (HangupEvent)event;
				
				for(Channel channel : channels){
					
					Channel.Descriptor descriptor = channel.getDescriptor();
					logger.info("found descriptor " + descriptor.getChannel() + " check hangupEvent[channel=" + hangupEvent.getChannel() + "]");
					
					if(hangupEvent.getChannel().equals(descriptor.getChannel())){
						
						logger.info("Hangup[" + descriptor.getId() +"] channels size " + channels.size());
						
						// change state when there's on only one left
						if(channels.size() == 0)
							setState(Call.INVALID_STATE,"Hangup");
						return true;
					}
				}
			}
			break;
		}
		return false;
	}

	@Override
	public void setState(int state, String reasonForStateChange) {
		logger.info("current state " + this.getState());
		stateChanged(this.getState(), this);
		this.state = state;
	}
	
	
}
