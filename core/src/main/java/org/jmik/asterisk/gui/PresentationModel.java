package org.jmik.asterisk.gui;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.jmik.asterisk.model.impl.Call;
import org.jmik.asterisk.model.impl.ConferenceCall;
import org.jmik.asterisk.model.impl.SinglePartyCall;
import org.jmik.asterisk.model.impl.TwoPartiesCall;

public class PresentationModel {
	
	private static Logger logger = Logger.getLogger(PresentationModel.class);
	
	public static final int SINGLEPARTY_CALLTYPE = 0;
	public static final int TWOPARTIES_CALLTYPE = 1;
	public static final int CONFERENCE_CALLTYPE = 2;
	
	List<PresentationModel.Listener> listeners;//AgiExp 
	List<Call> singlePartyCalls;
	List<Call> twoPartiesCalls;
	List<Call> conferenceCalls;
	
	public PresentationModel(){
		singlePartyCalls = new ArrayList<Call>();
		twoPartiesCalls = new  ArrayList<Call>();
		conferenceCalls = new ArrayList<Call>();
		listeners = new ArrayList<PresentationModel.Listener>();
		logger.info("PresentationModel");
	}
	
	public void addListener(PresentationModel.Listener listener){
		listeners.add(listener);
		logger.info("addListener " + listener);
	}

	public void removeListener(PresentationModel.Listener listener){
		listeners.remove(listener);
		logger.info("removeListener " + listener);
	}

	public List<Call> getCalls(int type){
		
		switch (type) {
			case Call.SINGLEPARTY_CALL:
				return singlePartyCalls;
			case Call.TWOPARTIES_CALL:
				return twoPartiesCalls;
			case Call.CONFERENCE_CALL:
				return conferenceCalls;
			default:
				throw new RuntimeException("unknown call type " + type);
		}
		
	}

	public void callAttached(Call call) {
		if(call instanceof SinglePartyCall){
			singlePartyCalls.add(call);
			logger.info("SinglePartyCall callAttached");
		}else if(call instanceof TwoPartiesCall){
			twoPartiesCalls.add(call);
			logger.info("TwoPartiesCall callAttached");
		}else if(call instanceof ConferenceCall){
			conferenceCalls.add(call);
			logger.info("ConferenceCall callAttached");
		}
		
		for(PresentationModel.Listener listener : listeners ){
			listener.callAttached(this, call);
			logger.info("notify callAttached to " + listener);
		}

	}

	public void callDetached(Call call) {
		if(call instanceof SinglePartyCall){
			singlePartyCalls.remove(call);
			logger.info("SinglePartyCall callDetached");
		}else if(call instanceof TwoPartiesCall){
			twoPartiesCalls.remove(call);
			logger.info("TwoPartiesCall callDetached");
		}else if(call instanceof ConferenceCall){
			conferenceCalls.remove(call);
			logger.info("ConferenceCall callDetached");
		}
		
		for(PresentationModel.Listener listener : listeners ){
			listener.callDetached(this, call);
			logger.info("notify callDetached to " + listener);
		}
	}

	public void callStateChanged(int oldState,Call call){
		
		if(singlePartyCalls.contains(call)){
			for(Call c : singlePartyCalls){
				if(c.getId().equals(call.getId())){
//					logger.info("singlePartyCalls callStateChanged oldState " + oldState  + " call " +call);					
					//TODO switch case for state select right one
//					if(c.getState() == oldState)
//						call.setState(Call.CONNECTING_STATE,"Connecting");
				}
			}
		}else if(twoPartiesCalls.contains(call)){
			for(Call c : twoPartiesCalls){
				if(c.getId().equals(call.getId())){
//					logger.info("twoPartiesCalls callStateChanged oldState " + oldState  + " call " +call);					
					//TODO switch case for state select right one
//					if(c.getState() == oldState)
//						call.setState(Call.CONNECTING_STATE,"Connecting");
				}
			}
		}else if(conferenceCalls.contains(call)){
			for(Call c : conferenceCalls){
				if(c.getId().equals(call.getId())){
//					logger.info("conferenceCalls callStateChanged oldState " + oldState  + " call " +call);					
					//TODO switch case for state select right one
//					if(c.getState() == oldState)
//						call.setState(Call.CONNECTING_STATE,"Connecting");
				}
			}
		}
	}
	
	public void dropButtonClicked(int i){
		
	}
	
	public static interface Listener {
		public void callAttached(PresentationModel presentationModel,Call call);
		public void callDetached(PresentationModel presentationModel, Call call);
	}

	public List<PresentationModel.Listener> getListeners() {
		return listeners;
	}

	
}
