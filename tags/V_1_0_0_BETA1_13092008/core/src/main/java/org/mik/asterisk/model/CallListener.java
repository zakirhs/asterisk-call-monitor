package org.mik.asterisk.model;

import org.mik.asterisk.model.call.Call;
import org.mik.asterisk.model.call.impl.Channel;
import org.mik.asterisk.model.call.impl.ConferenceCall;

public interface CallListener {
	public void stateChanged(int oldState, Call call);
	public void channelAdded(ConferenceCall conferenceCall, Channel channel);
	public void channelRemoved(ConferenceCall conferenceCall, Channel channel);
}
