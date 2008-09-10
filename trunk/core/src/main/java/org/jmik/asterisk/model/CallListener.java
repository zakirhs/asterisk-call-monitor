package org.jmik.asterisk.model;

import org.jmik.asterisk.model.impl.Call;
import org.jmik.asterisk.model.impl.Channel;
import org.jmik.asterisk.model.impl.ConferenceCall;

/**
 * Class implementing this interface will be notify 
 * of call's state changes.
 * 
 * @author La Porta
 *
 */
public interface CallListener {
	
	/**
	 * 
	 * @param newState
	 * @param call
	 */
	public void stateChanged(int newState,Call call);
	
	/**
	 * 
	 * @param conferenceCall
	 * @param channel
	 */
	public void channelAdded(ConferenceCall conferenceCall,Channel channel);
	
	/**
	 * 
	 * @param conferenceCall
	 * @param channel
	 */
	public void channelRemoved(ConferenceCall conferenceCall,Channel channel);
	
}
