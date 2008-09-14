package org.jmik.asterisk;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.asteriskjava.manager.AuthenticationFailedException;
import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerConnectionFactory;
import org.asteriskjava.manager.TimeoutException;
import org.jmik.asterisk.gui.PresentationModel;
import org.jmik.asterisk.model.CallListener;
import org.jmik.asterisk.model.Provider;
import org.jmik.asterisk.model.ProviderListener;
import org.jmik.asterisk.model.agi.AgiExp;
import org.jmik.asterisk.model.constants.Constants;
import org.jmik.asterisk.model.impl.AsteriskProvider;
import org.jmik.asterisk.model.impl.Call;
import org.jmik.asterisk.model.impl.Channel;
import org.jmik.asterisk.model.impl.ConferenceCall;
import org.jmik.asterisk.model.impl.SinglePartyCall;
import org.jmik.asterisk.model.impl.TwoPartiesCall;
import org.jmik.asterisk.monitor.ConferenceCallMonitor;
import org.jmik.asterisk.monitor.ConferenceMonitorAgiServer;
import org.jmik.asterisk.monitor.ConferenceMonitorScript;

/**
 * 
 * @author Michele La Porta
 *
 */
public class Main implements ProviderListener, CallListener {

	private static Logger logger = Logger.getLogger(Main.class);
	private static PresentationModel presentationModel;

	private AsteriskProvider asteriskProvider;
	private List<Call> conferenceCalls;
	private List<Call> singlePartyCalls;
	private List<Call> twoPartiesCalls;
	
	/**
	 * Main ProviderListener
	 * @param host
	 * @param port
	 * @param userid
	 * @param password
	 */
	public Main(String host,int port,String userid,String password) {
		
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
		
		singlePartyCalls = new ArrayList<Call>();
		twoPartiesCalls = new ArrayList<Call>();
		conferenceCalls = new ArrayList<Call>();

		presentationModel = new PresentationModel(asteriskProvider,singlePartyCalls,twoPartiesCalls,conferenceCalls);

	}

	public void callAttached(Call call) {
		
		if(call instanceof SinglePartyCall){
			singlePartyCalls.add(call);
		}else if(call instanceof TwoPartiesCall){
			twoPartiesCalls.add(call);
		}else if(call instanceof ConferenceCall){
			conferenceCalls.add(call);
			logger.info("ConferenceCall callAttached " + call);
			}
		call.addListener(this);
		
		presentationModel.callAttached(call);
		logger.info("callAttached " + call);
				
	}
	
	public void callDetached(Call call) {
		
		if(call instanceof SinglePartyCall){
			singlePartyCalls.remove(call);
			logger.info("callDetached " + singlePartyCalls);
		}else if(call instanceof TwoPartiesCall){
			twoPartiesCalls.remove(call);
			logger.info("callDetached " + twoPartiesCalls);
		}else if(call instanceof ConferenceCall){
			conferenceCalls.remove(call);
			logger.info("callDetached " + conferenceCalls);
		}
		
//		presentationModel.callDetached(call);
		call.removeListener(this);
	}


	public void stateChanged(int oldState, Call call) {
		
		logger.info("stateChanged " + call + " State " + call.getState() + " oldState " + oldState);
		
		presentationModel.callStateChanged(oldState, call);
		
		if(call.getState() == Call.INVALID_STATE){
			call.removeListener(this);

			if(call instanceof SinglePartyCall){
				singlePartyCalls.remove(call);
				logger.info("remove from singlePartyCalls");
			}else if(call instanceof TwoPartiesCall){
				twoPartiesCalls.remove(call);
				logger.info("remove from twoPartiesCalls");
			}else if(call instanceof ConferenceCall){
				conferenceCalls.remove(call);
				logger.info("remove from conferenceCalls");
			}

//			presentationModel.callDetached(call);
		}
	}
	
	
	public void channelAdded(ConferenceCall conferenceCall, Channel channel) {
		presentationModel.channelAdded(conferenceCall, channel);
	}
	public void channelRemoved(ConferenceCall conferenceCall, Channel channel) {
		presentationModel.channelRemoved(conferenceCall, channel);
	}
	public List<Call> getConferenceCalls() {
		return conferenceCalls;
	}

	public List<Call> getSinglePartyCalls() {
		return singlePartyCalls;
	}

	public List<Call> getTwoPartiesCalls() {
		return twoPartiesCalls;
	}

//	public PresentationModel getPresentationModel() {
//		return presentationModel;
//	}

	public Provider getAsteriskProvider() {
		return asteriskProvider;
	}

	public void run(final String host,final int port,final String confMonitorIp,final int confMonitorPort){

		Thread conferenceMonitorThread = new Thread(new Runnable() {
			public void run() {
				
				ConferenceCallMonitor conferenceCallMonitor = new ConferenceCallMonitor(Constants.conferenceMonitorIpAddress, 
						Constants.conferenceMonitorPort, Constants.asteriskIpAddress, Constants.asteriskPort);
				asteriskProvider.addListener(conferenceCallMonitor);
				
				ConferenceMonitorScript monitorScript = new ConferenceMonitorScript(
						conferenceCallMonitor);
				ConferenceMonitorAgiServer agiServer = new ConferenceMonitorAgiServer(
						monitorScript);
				try {
					logger.info("agiServer starting ");
					agiServer.startup();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}			
			}
		});
		logger.info("conferenceMonitorThread starting");
		conferenceMonitorThread.start();
		
	}

	public static void main(final String[] args) {
		
		final Main mainFrame = new Main(Constants.asteriskIpAddress,Constants.asteriskPort,Constants.asteriskManagerUser,Constants.asteriskManagerPassword);
		
		Thread inputThread  = new Thread(new Runnable(){		
			public void run() {
				try {
					java.io.InputStreamReader reader = new java.io.InputStreamReader(System.in);
					java.io.BufferedReader myInput = new java.io.BufferedReader(reader);
					String str = new String();
					try {
						while((str = myInput.readLine()) != null){
							System.out.println("" + str);
							if(str.equals("print")){
								System.out.println("main singlePartyCall " + mainFrame.getSinglePartyCalls());
								System.out.println("main twopartiesCall " + mainFrame.getTwoPartiesCalls());
								System.out.println("main conferenceCall " + mainFrame.getConferenceCalls());
								System.out.println("main callconstuction " + mainFrame.getAsteriskProvider().getCallConstrutions());
//								System.out.println("singlePartyMonitor " + singlePartyMonitor.getSinglePartyCalls());
//								System.out.println("twoPartiesMonitor " + twoPartiesMonitor.getTwoPartiesCalls());
//								System.out.println("conferenceMonitor " + conferenceMonitor.getConferenceCalls());
							}else if(str.equals("help")){
								System.out.println("usage\n");
								System.out.println("print");
							}
						}
					} catch (java.io.IOException e) {
						System.out.println("Si è verificato un errore: " + e);
						System.exit(-1);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		inputThread.start();

		final JFrame frame = new AgiExp(presentationModel/*,mainFrame.getAsteriskProvider()*/);
		
		Thread mainThread  = new Thread(new Runnable(){		
			public void run() {
				try {
					mainFrame.run(Constants.asteriskIpAddress,Constants.asteriskPort, Constants.conferenceMonitorIpAddress, Constants.conferenceMonitorPort);
					
				} catch (Exception e) {
					JOptionPane.showMessageDialog(null, "Error:" + e.getMessage());
					System.exit(-1);
				}
			}
		});
		mainThread.start();
		
		logger.info("ready");
		// Schedule a job for the event-dispatching thread:
        // creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable(){
			public void run() {
				frame.setVisible(true);
			}
		});
	}

}
