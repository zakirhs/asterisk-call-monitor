package org.mik.asterisk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;
import org.mik.asterisk.model.CallListener;
import org.mik.asterisk.model.ProviderListener;
import org.mik.asterisk.model.call.Call;
import org.mik.asterisk.model.call.impl.Channel;
import org.mik.asterisk.model.call.impl.ConferenceCall;
import org.mik.asterisk.model.call.impl.SinglePartyCall;
import org.mik.asterisk.model.call.impl.TwoPartiesCall;
import org.mik.asterisk.model.constant.Constants;
import org.mik.asterisk.model.gui.AgiExp;
import org.mik.asterisk.model.impl.AsteriskProvider;
import org.mik.asterisk.model.impl.PresentationModel;
import org.mik.asterisk.model.monitor.ConfMonitorAgiServer;
import org.mik.asterisk.model.monitor.ConferenceMonitor;
import org.mik.asterisk.model.monitor.ConferenceMonitorScript;

public class Main implements ProviderListener, CallListener {

	private AsteriskProvider asteriskProvider;
	
	private List singlePartyCalls;
	private List twoPartiesCalls;
	private List conferenceCalls;
	
	private PresentationModel presentationModel;
	private AgiExp ui;
	
	public Main(String host,int port,String userid,String password) throws Exception {
		
		singlePartyCalls = new ArrayList();
		twoPartiesCalls = new ArrayList();
		conferenceCalls = new ArrayList();		

				// 1 open a connection to Asterisk
		ManagerConnectionFactory managerConnectionFactory = new ManagerConnectionFactory(host,port,userid,password);
		ManagerConnection managerConnection = managerConnectionFactory.createManagerConnection();
		asteriskProvider = new AsteriskProvider(managerConnection);
		
		// 2 register to Asterisk Provider
		asteriskProvider.addListener(this);
		
		// 3 connect to Asterisk and log in
		try {
			managerConnection.login();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (AuthenticationFailedException e) {
			e.printStackTrace();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		
		// 4 register AsteriskProvider as the handler of manager events
		managerConnection.addEventListener(asteriskProvider);
		
//		managerConnectionFactory = new ManagerConnectionFactory(host,port,userid,password);
//		managerConnection = managerConnectionFactory.createManagerConnection(); 	
//		asteriskProvider = new AsteriskProvider(mgrConn);		
//		asteriskProvider.addListener(this);
//		mgrConn.addEventHandler(asteriskProvider);
//		mgrConn.login();
		
		
		presentationModel = new PresentationModel(asteriskProvider, singlePartyCalls, 
			twoPartiesCalls, conferenceCalls);
		ui = new AgiExp(presentationModel);		
	}
	
	public void run(final String asteriskIP, final int asteriskSIPPort, 
		final String confMonitorIP, final int confMonitorPort) {
		try {
			Thread agiServerThread = new Thread(new Runnable() {
				public void run() {				
					ConferenceMonitor confMonitor = new ConferenceMonitor(confMonitorIP, 
						confMonitorPort, asteriskIP, asteriskSIPPort);					
					asteriskProvider.addListener(confMonitor);					
					ConferenceMonitorScript confMonitorScript = 
						new ConferenceMonitorScript(confMonitor);
					ConfMonitorAgiServer confMonitorAgiServer = 
						new ConfMonitorAgiServer(confMonitorScript);					
					confMonitorAgiServer.run();
				}
			});			
			agiServerThread.start();			
		} catch (Exception e) {
			JOptionPane.showMessageDialog(ui, "Error: " + e.getLocalizedMessage());
			System.exit(-1);
		}
	}
	
	public void makeUIVisible() {
		ui.setVisible(true);
	}

	public void callAttached(Call call) {		
		if(call instanceof SinglePartyCall) {
			singlePartyCalls.add(call);			
		} else if(call instanceof TwoPartiesCall) {
			twoPartiesCalls.add(call);
		} else if(call instanceof ConferenceCall) {
			conferenceCalls.add(call);
		}  
		
		call.addListener(this);
		presentationModel.callAttached(call);
	}
	
	public void callDetached(Call call) {}

	public void stateChanged(int oldState, Call call) {
		presentationModel.callStateChanged(oldState, call);
		if(call.getState() == Call.INVALID_STATE) call.removeListener(this);
	}
	
	public void channelAdded(ConferenceCall conferenceCall, Channel channel) {
		presentationModel.channelAdded(conferenceCall, channel);
	}
	public void channelRemoved(ConferenceCall conferenceCall, Channel channel) {
		presentationModel.channelRemoved(conferenceCall, channel);
	}
	
	public static void main(final String[] args) throws Exception {
		final Main main = new Main(Constants.asteriskIpAddress,Constants.asteriskPort,Constants.asteriskManagerUser,Constants.asteriskManagerPassword);
		
		Thread runner1 = new Thread(new Runnable() {
			public void run() {
				try {
					main.run(Constants.asteriskIpAddress, Constants.asteriskPort,Constants.conferenceMonitorIpAddress,Constants.conferenceMonitorPort);	
				} catch (Exception e) {
//					JOptionPane.showMessageDialog(null, "Error: " + e.getLocalizedMessage());
//					System.exit(-1);
					e.printStackTrace();
				}
			}
		});
		runner1.start();
		
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
            	main.makeUIVisible();
            }
        });
	}	
}
