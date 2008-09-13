package org.jmik.asterisk.model.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.log4j.Logger;
import org.asteriskjava.manager.event.HangupEvent;
import org.asteriskjava.manager.event.ManagerEvent;
import org.asteriskjava.manager.event.MeetMeLeaveEvent;
import org.jmik.asterisk.model.CallListener;
import org.jmik.asterisk.model.impl.Channel.Descriptor;
import org.jmik.asterisk.model.utils.DateFormatter;

/**
 * This class represents a call with more than two participant (thus more than
 * two channels) In Asterisk model conference it's more like a collection of
 * single party calls which are destinated to an extension which is configured
 * to switch the call to a particular conference room,where the information from
 * the callers are mixed and then ditributed to every participants. In this
 * JTAPI model a Call has a set of instances of Connection therefore in our
 * model a ConferenceCall is modeled to have a set of instances of Channel.
 * 
 * @author Michele La Porta
 * 
 */

public class ConferenceCall extends Call {

	private static Logger logger = Logger.getLogger(ConferenceCall.class);

	private Map<Channel.Descriptor, Channel> channels;
	private String roomId;

	public ConferenceCall(String roomId, Date date, Descriptor channelDesc) {
		
		super(roomId + "|" + DateFormatter.format(channelDesc.getCreationTime()), date,
				Call.ACTIVE_STATE);
		
		if (roomId == null){
			logger.error("roomId is null");
			throw new IllegalArgumentException("roomId can not be null");
		}
		if (channelDesc == null){
			logger.error("channelDesc can not be null");
			throw new IllegalArgumentException("channelDesc can not be null");
		}

		this.roomId = roomId;
		channels = new HashMap<Channel.Descriptor, Channel>();
		addChannel(channelDesc);
		logger.info("CallConference state " + this.getState());
	}

	public java.util.Set<Channel> getChannels() {
		return new LinkedHashSet<Channel>(channels.values());
	}

	public void addChannel(Descriptor channelDescriptor) {

		if (channelDescriptor == null)
			throw new AssertionError("channelDescriptor can not be null");
		Channel channel = new Channel(channelDescriptor, this);
		channels.put(channelDescriptor, channel);
		logger.info("add channel " + channel);

		for (CallListener callListener : listeners) {
			callListener.channelAdded(this, channel);
			logger.info("addChannel notify call listener <" + callListener
					+ "> of state changed");
		}
		logger.info("addChannel channels size " + channels.size());

	}

	public void removeChannel(Descriptor channelDescriptor) {

		if (channelDescriptor == null) {
			logger.error("channelDescriptor is null");
			throw new AssertionError("channel can not be null");
		}
		if (channels.containsKey(channelDescriptor)) {
			logger.info("found channelDescriptor " + channelDescriptor);

			Channel channel = (Channel) channels.remove(channelDescriptor);
			logger.info("remove channel " + channel);

			logger.info("listeners size " + listeners.size());
			for (CallListener callListener : listeners) {
				callListener.channelRemoved(this, channel);
				logger.info("removeChannel notify call listener <"
						+ callListener + "> of state changed");
			}
		}

		if (channels.size() == 0) {
			setState(Call.INVALID_STATE, "All Parties Left");
			logger.info("All Parties Left");

		}
	}

	public boolean process(ManagerEvent event) {
		
		logger.debug("CallConference[state=" + getState()+ "] processing " + event.getClass().getSimpleName());
	
		if (getState() == Call.ACTIVE_STATE) {
			
			if (event instanceof MeetMeLeaveEvent) {
				MeetMeLeaveEvent mmle = (MeetMeLeaveEvent) event;
				logger.info("MeetMeLeaveEvent[" + mmle.getChannel()+"]");
				
				logger.info("channels " + channels.size());
				
				for (Iterator iter = channels.keySet().iterator(); iter
						.hasNext();) {
					Channel.Descriptor channelDesc = (Channel.Descriptor) iter
							.next();
					logger.info("verify channelDesc " + channelDesc.getId() + " mmle.getChannel() " + mmle.getChannel());

					if (channelDesc.getId().equals(mmle.getChannel())) {
						removeChannel(channelDesc);
						return true;
					}
				}
			}/*else if(event instanceof HangupEvent){
				
				HangupEvent hangupEvent = (HangupEvent)event;
				
				// change state when there's on only one left
				if(channels.size() == 0)
					setState(Call.INVALID_STATE,"Hangup");
				return true;
				
			}*/else
				logger.debug("NOT PROCESSED event " +event.getClass().getSimpleName());
			
		}

		return false;
	}
	// @Override
	// public boolean process(ManagerEvent event) {
	//		
	// switch (state) {
	//		
	// case Call.IDLE_STATE:
	//			
	// logger.debug("process " + event);
	// if(event instanceof MeetMeJoinEvent){
	// MeetMeJoinEvent meetMeJoinEvent = (MeetMeJoinEvent)event;
	// // // new one?no will be added by call construction if conference exists
	// // Channel.Descriptor descriptor = new
	// Channel.Descriptor(meetMeJoinEvent.getChannel(),meetMeJoinEvent.getDateReceived(),new
	// CallEndpoint("" + meetMeJoinEvent.getUserNum()));
	// // this.addChannel(descriptor);
	// // logger.info("IDLE MeetMeJoinEvent channels size " + channels.size());
	//				
	// // logger.info("meetMeJoinEvent add " + descriptor.getChannel() +"
	// channels " + channels);
	// if(meetMeJoinEvent.getMeetMe().equals(this.getId())){
	// setState(Call.ACTIVE_STATE, "MeetMeJoinEvent");
	// logger.info("meetMeJoinEvent ACTIVE_STATE " + channels.size());
	// return false;
	// }
	//				
	// }/*else if(event instanceof NewChannelEvent){
	// NewChannelEvent newChannelEvent = (NewChannelEvent)event;
	// logger.info("newChannelEvent " + newChannelEvent.getChannel());
	// // String matcher =
	// newChannelEvent.getChannel().substring(0,newChannelEvent.getChannel().indexOf("-"));
	// //
	// // if(matcher.equals(calledChannel.getDescriptor().getId())){
	// // // my event set as processed;
	// // calledChannel.getDescriptor().setId(newChannelEvent.getChannel());
	// // logger.info("my destination channel " +
	// calledChannel.getDescriptor().getId());
	// // return true;
	// // }
	// }*/
	//
	// case Call.ACTIVE_STATE:
	//
	// if(event instanceof MeetMeLeaveEvent){
	//				
	// MeetMeLeaveEvent meetMeLeaveEvent = (MeetMeLeaveEvent)event;
	//				
	// // iterate over descriptors to find a matching channel
	// for(Channel channel : channels){
	//					
	// Channel.Descriptor descriptor = channel.getDescriptor();
	// logger.info("found descriptor " + descriptor.getChannel() + " check
	// meetMeLeaveEvent[channel=" + meetMeLeaveEvent.getChannel() + "]");
	//					
	// if(meetMeLeaveEvent.getChannel().equals(descriptor.getChannel())){
	// this.removeChannel(descriptor);
	// if(channels.size() == 0)
	// setState(Call.INVALID_STATE, "Conference is empty");
	// return true;
	// }
	// }
	//				
	//				
	// }else if(event instanceof HangupEvent){
	//				
	// HangupEvent hangupEvent = (HangupEvent)event;
	//				
	// for(Channel channel : channels){
	//					
	// Channel.Descriptor descriptor = channel.getDescriptor();
	// logger.info("found descriptor " + descriptor.getChannel() + " check
	// hangupEvent[channel=" + hangupEvent.getChannel() + "]");
	//					
	// if(hangupEvent.getChannel().equals(descriptor.getChannel())){
	//						
	// logger.info("Hangup[" + descriptor.getId() +"] channels size " +
	// channels.size());
	//						
	// // change state when there's on only one left
	// if(channels.size() == 0)
	// setState(Call.INVALID_STATE,"Hangup");
	// return true;
	// }
	// }
	// }
	// break;
	// }
	// return false;
	// }

	public String getRoomId() {
		return roomId;
	}

	// @Override
	// public void setState(int state, String reasonForStateChange) {
	// logger.info("current state " + this.getState());
	//		stateChanged(this.getState(), this);
	//		this.state = state;
	//	}

}
