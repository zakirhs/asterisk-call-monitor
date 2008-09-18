package org.jmik.asterisk.model;

import org.jmik.asterisk.model.impl.Call;
import org.jmik.asterisk.model.impl.Channel;
import org.jmik.asterisk.model.impl.ConferenceCall;

/**
 * Class implementing this interface will be notify 
 * of call's state changes.
 * 
 * @author Michele La Porta
 *
 */
public interface CallListener {
	
	/**
	 * Notification of Call state changed.
	 * @param newState
	 * @param call
	 */
	public void stateChanged(int newState,Call call);
	
	/**
	 * Notification of Channel added on ConferenceCall.
	 * @param conferenceCall
	 * @param channel
	 */
	public void channelAdded(ConferenceCall conferenceCall,Channel channel);
	
	/**
	 * Notification of Channel removed on ConferenceCall.
	 * @param conferenceCall
	 * @param channel
	 */
	public void channelRemoved(ConferenceCall conferenceCall,Channel channel);
	
}
