package org.jmik.asterisk.model.agi;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;
import org.jmik.asterisk.gui.ConferenceCallsJPanel;
import org.jmik.asterisk.gui.PresentationModel;
import org.jmik.asterisk.gui.SinglePartyCallsJPanel;
import org.jmik.asterisk.gui.TwoPartiesCallsJPanel;
import org.jmik.asterisk.model.Provider;
import org.jmik.asterisk.model.impl.Call;
import org.jmik.asterisk.model.impl.ConferenceCall;
import org.jmik.asterisk.model.impl.SinglePartyCall;
import org.jmik.asterisk.model.impl.TwoPartiesCall;

/**
 * This class is a Frame with three tab one for each kind of Call type.
 * Receive call attached/detached notification from PresentationModel and update tabs.
 * Can drop/monitor a Call through Provider.
 *  
 * @author Michele La Porta
 *
 */
public class AgiExp extends JFrame implements PresentationModel.Listener, ChangeListener{

	private static final long serialVersionUID = 1158448031534008968L;

	private static Logger logger = Logger.getLogger(AgiExp.class);

	private PresentationModel presentationModel;
	private Provider provider;
	private JTabbedPane jTabbedPane;
	private SinglePartyCallsJPanel singlePartyCallsJPanel;
	private TwoPartiesCallsJPanel twoPartiesCallsJPanel;
	private ConferenceCallsJPanel conferenceCallsJPanel;
	
	public AgiExp(PresentationModel presentationModel,Provider provider) {
		this.provider = provider;
		this.presentationModel = presentationModel;
		presentationModel.addListener(this);
		
		setTitle("Asterisk Call Manager");
		setSize(700, 500);
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				System.exit(0);
			}
		});

		jTabbedPane = new JTabbedPane();
		
		singlePartyCallsJPanel = new SinglePartyCallsJPanel(provider);
		twoPartiesCallsJPanel = new TwoPartiesCallsJPanel(provider);
		conferenceCallsJPanel = new ConferenceCallsJPanel(provider);
		
		presentationModel.addListener(singlePartyCallsJPanel);
		presentationModel.addListener(twoPartiesCallsJPanel);
		presentationModel.addListener(conferenceCallsJPanel);
		
		jTabbedPane.addChangeListener(this);

		jTabbedPane.addTab("SinglePartyCall",singlePartyCallsJPanel );
		jTabbedPane.addTab("TwoPartiesCall", twoPartiesCallsJPanel);
		jTabbedPane.addTab("ConferenceCall", conferenceCallsJPanel);
		
		getContentPane().add(jTabbedPane, "Center");
	}

	public void fireDataChanged(Object model, int type) {
		
		logger.info("fireDataChanged model=" + model + " type="+type);
		
		if(model instanceof PresentationModel){

			PresentationModel pm = (PresentationModel)model;

			if(type == Call.SINGLEPARTY_CALL ){
				for(PresentationModel.Listener presentationModelListener : pm.getListeners()){
		//			singlePartyPresentationModelImpl.fireTableDataChanged();//(singlePartyCallsJPanel.getSinglePartyTableModel())
					logger.info("fireTableDataChanged " + presentationModelListener + " " + pm.getCalls(Call.SINGLEPARTY_CALL));
//					presentationModellisteners.callAttached(pm, call)
				}
				
				
			}else if(type == Call.TWOPARTIES_CALL ){
				logger.info("fireTableDataChanged " +pm.getCalls(Call.SINGLEPARTY_CALL));
				
			}else if(type == Call.CONFERENCE_CALL ){
				logger.info("fireTableDataChanged " +pm.getCalls(Call.SINGLEPARTY_CALL));
				
			}
		}
		
	}

	
	public void callAttached(PresentationModel presentationModel,Call call) {
		logger.info("callAttached presentationModel=" + presentationModel + " call="+call);
		
		if(call instanceof SinglePartyCall){
			fireDataChanged(presentationModel,PresentationModel.SINGLEPARTY_CALLTYPE);
			
		}else if(call instanceof TwoPartiesCall){
			fireDataChanged(presentationModel,PresentationModel.TWOPARTIES_CALLTYPE);
			
		}else if(call instanceof ConferenceCall){
			fireDataChanged(presentationModel,PresentationModel.CONFERENCE_CALLTYPE);
			
		}
	}

	public void callDetached(PresentationModel presentationModel, Call call) {
		logger.info("callDetached presentationModel=" + presentationModel + " call="+call);
			
		if(call instanceof SinglePartyCall){
			fireDataChanged(presentationModel,PresentationModel.SINGLEPARTY_CALLTYPE);
			
		}else if(call instanceof TwoPartiesCall){
			fireDataChanged(presentationModel,PresentationModel.TWOPARTIES_CALLTYPE);
			
		}else if(call instanceof ConferenceCall){
			fireDataChanged(presentationModel,PresentationModel.CONFERENCE_CALLTYPE);
			
		}
	}
	
	public void drop(Call call){
		provider.drop(call);
	}

	public void stateChanged(ChangeEvent event) {
		try {
			logger.info("stateChanged=" + event);
			
//			JTabbedPane jtabbedPanel = (JTabbedPane)event.getSource();
////			JTabbedPane pane = (JTabbedPane) agiExp.getTabbedPane();
//			logger.info("jtabbedPanel " + jtabbedPanel);
//			
//			for(Component jtabbedPanelChild : jtabbedPanel.getComponents()){
//				JPanel jPanel = (JPanel)jtabbedPanelChild;
//				logger.info("jPanel " + jtabbedPanelChild);
//				
//				for(Component jpanelChild :jPanel.getComponents()){
//					logger.info("jpanelChild " + jpanelChild);
//					if(jpanelChild instanceof JScrollPane){
//						JScrollPane jScrollPane = (JScrollPane)jpanelChild;
//						
////						jScrollPane.get
////
////						JViewport jViewport = jScrollPane.getViewport();
////						logger.info("jViewport " + jViewport);
//						
////						for(Component jScrollPaneChild :jScrollPane.getComponents()){
////							logger.info("jScrollPaneChild " + jScrollPaneChild);
////						}
//					}
//					
//				}
//					
//			}
			
			// check if this tab still has a null component
	
//			if (jtabbedPanel.getSelectedComponent() == null) { // set the component to the
//														// image icon
//	
//				int n = jtabbedPanel.getSelectedIndex();
//				String title = jtabbedPanel.getTitleAt(n);
//				ImageIcon planetIcon = new ImageIcon(title + ".gif");
//				jtabbedPanel.setComponentAt(n, new JLabel(planetIcon));
//	
//				// indicate that this tab has been visited--just for fun
//	
//				jtabbedPanel.setIconAt(n, new ImageIcon("red-ball.gif"));
//				logger.info("indicate that this tab has been visited--just for fun");
//				
//			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	class SinglePartyCallsTableModel extends PresentationModel implements TableModel{
		
		PresentationModel presentationModel;
		
		
		public SinglePartyCallsTableModel(PresentationModel presentationModel){
			this.presentationModel = presentationModel;
		}
		
		public void addTableModelListener(TableModelListener tableModelListener) {
			this.addTableModelListener(tableModelListener);
			
		}

		public Class<?> getColumnClass(int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public int getColumnCount() {
			// TODO Auto-generated method stub
			return 0;
		}

		public String getColumnName(int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public int getRowCount() {
			return presentationModel.getCalls(PresentationModel.SINGLEPARTY_CALLTYPE).size();
		}

		public Object getValueAt(int rowIndex, int columnIndex) {
			SinglePartyCall call = (SinglePartyCall)presentationModel.getCalls(PresentationModel.SINGLEPARTY_CALLTYPE).get(rowIndex);
			
			switch (columnIndex) {
				case 0:
					return call.getId();
				case 1:
					return call.getChannel().getDescriptor().getEndpoint().getCallId();
				case 2:
					switch (call.getState()) {
						case Call.IDLE_STATE:return "IDLE";
						case Call.CONNECTING_STATE:return "CONNECTING";
						case Call.ACTIVE_STATE:return "ACTIVE";
						case Call.INVALID_STATE:return "INVALID";
					}
				case 3:
					return call.getReasonForStateChange();
			}
			
			return null;
		}

		public boolean isCellEditable(int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
			return false;
		}

		public void removeTableModelListener(TableModelListener l) {
			// TODO Auto-generated method stub
		}

		public void setValueAt(Object value, int rowIndex, int columnIndex) {
			// TODO Auto-generated method stub
		}
	}*/
	

	public Provider getProvider() {
		return provider;
	}

	public PresentationModel getPresentationModel() {
		return presentationModel;
	}

	public SinglePartyCallsJPanel getSinglePartyCallsJPanel() {
		return singlePartyCallsJPanel;
	}

	public TwoPartiesCallsJPanel getTwoPartiesCallsJPanel() {
		return twoPartiesCallsJPanel;
	}

	public ConferenceCallsJPanel getConferenceCallsJPanel() {
		return conferenceCallsJPanel;
	}

	
}
