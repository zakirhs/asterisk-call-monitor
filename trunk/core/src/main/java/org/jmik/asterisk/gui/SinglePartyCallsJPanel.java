package org.jmik.asterisk.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;
import org.jmik.asterisk.model.Provider;
import org.jmik.asterisk.model.impl.Call;
import org.jmik.asterisk.model.impl.Channel;
import org.jmik.asterisk.model.impl.ConferenceCall;
import org.jmik.asterisk.model.impl.SinglePartyCall;
import org.jmik.asterisk.model.impl.TwoPartiesCall;

/**
 * This class is a JPanel with a single party calls table model.
 *  
 * @author Michele La Porta
 *
 */
public class SinglePartyCallsJPanel extends JPanel implements PresentationModel.Listener{
	
	private static final long serialVersionUID = -192017635119315641L;

	private static Logger logger = Logger.getLogger(SinglePartyCallsJPanel.class);

	private JButton monitorButton = new JButton("monitor") ;
	private JButton dropButton = new JButton("drop");
	private JTable table;
	private SinglePartyTableModel singlePartyTableModel;
	private Provider provider;

	public SinglePartyCallsJPanel(Provider provider){
		this.provider = provider;
		
		setLayout(new BorderLayout());
		setBackground(Color.yellow);
		
		singlePartyTableModel = new SinglePartyTableModel();
		
		table = new JTable(singlePartyTableModel);
		
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));

        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);

        //Add the scroll pane to this panel.
        add(scrollPane,BorderLayout.CENTER);
		
		JPanel southPanel = new JPanel();
		southPanel.setLayout(new FlowLayout());
		
		monitorButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(table.getSelectedRow() != -1){
					int row = table.getSelectedRow();
					String callId = (String)table.getValueAt(row, 0);
					fireMonitor(callId);
				}
				
			}
		});
		southPanel.add(monitorButton);

		dropButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(table.getSelectedRow() != -1){
					int row = table.getSelectedRow();
					String callId = (String)table.getValueAt(row, 0);
					fireDrop(callId);
				}
			}
		});
		southPanel.add(dropButton);

		add(southPanel,BorderLayout.SOUTH);
		
		logger.info("SinglePartyCallsJTabbedPane " + this);
		
	}

	public void fireMonitor(String callId) {
//		provider.monitor(callId);
		logger.info("todo fireMonitor " + callId);
	}
	
	public void fireDrop(String callId) {
//		provider.drop(callId);
		logger.info("todo fireDrop " + callId);
	}
	
	public void fireDataChanged(PresentationModel presentationModel,Call call,int type,boolean attached) {
		logger.info("fireDataChanged=" + presentationModel +" type " +type);
		
		logger.info("singlePartyCall size = " + presentationModel.getCalls(PresentationModel.SINGLEPARTY_CALLTYPE).size());
		
		if(singlePartyTableModel.getRowCount() == 1 && attached){
			
			singlePartyTableModel.setValueAt(call.getId(), 0, 0);
			singlePartyTableModel.setValueAt(call.getChannel().getDescriptor().getId(), 0, 1);
			singlePartyTableModel.setValueAt(call.getState(), 0, 2);
			singlePartyTableModel.setValueAt(call.getReasonForStateChange(), 0, 3);			
		}else if(!attached){
			singlePartyTableModel.setValueAt("", 0, 0);
			singlePartyTableModel.setValueAt("", 0, 1);
			singlePartyTableModel.setValueAt("", 0, 2);
			singlePartyTableModel.setValueAt("", 0, 3);			
			
		}
	}
			
	public void callAttached(PresentationModel presentationModel,Call call) {
		
		logger.info("callAttached presentationModel=" + presentationModel + " call="+call);
		if(call instanceof SinglePartyCall){
			fireDataChanged(presentationModel,call,PresentationModel.SINGLEPARTY_CALLTYPE,true);
		}else if(call instanceof TwoPartiesCall){
			fireDataChanged(presentationModel,call,PresentationModel.TWOPARTIES_CALLTYPE,true);
		}else if(call instanceof ConferenceCall){
			fireDataChanged(presentationModel,call,PresentationModel.CONFERENCE_CALLTYPE,true);
		}
	}

	public void callDetached(PresentationModel presentationModel, Call call) {
		
		logger.info("callDetached presentationModel=" + presentationModel + " call="+call);
		if(call instanceof SinglePartyCall){
			fireDataChanged(presentationModel,call,PresentationModel.SINGLEPARTY_CALLTYPE,false);
		}else if(call instanceof TwoPartiesCall){
			fireDataChanged(presentationModel,call,PresentationModel.TWOPARTIES_CALLTYPE,false);
		}else if(call instanceof ConferenceCall){
			fireDataChanged(presentationModel,call,PresentationModel.CONFERENCE_CALLTYPE,false);
		}
	}
	
    class SinglePartyTableModel extends AbstractTableModel {
    	
    	private static final long serialVersionUID = -4092526950355852166L;

		private Logger log = Logger.getLogger(SinglePartyTableModel.class);
    	
		private String[] columnNames = { "ID", "Participants", "State","Reason" };
		
		private Object[][] data = new Object[][] {{"","","",""}};
		
		public int getColumnCount() {
			return columnNames.length;
		}

		public int getRowCount() {
			return data.length;
		}

		public String getColumnName(int col) {
			return columnNames[col];
		}

		public Object getValueAt(int row, int col) {
			return data[row][col];
		}

		/*
		 * JTable uses this method to determine the default renderer/
		 * editor for each cell.  If we didn't implement this method,
		 * then the last column would contain text ("true"/"false"),
		 * rather than a check box.
		 */
		public Class getColumnClass(int c) {
			return getValueAt(0, c).getClass();
		}

		/*
		 * Don't need to implement this method unless your table's
		 * editable.
		 */
		public boolean isCellEditable(int row, int col) {
			//Note that the data/cell address is constant,
			//no matter where the cell appears onscreen.
			if (col < 2) {
				return false;
			} else {
				return true;
			}
		}

		/*
		 * Don't need to implement this method unless your table's
		 * data can change.
		 */
		public void setValueAt(Object value, int row, int col) {
			log.info("Setting value at " + row + "," + col + " to "
					+ value + " (an instance of " + value.getClass() + ")");

			data[row][col] = value;
			fireTableCellUpdated(row, col);

			log.info("New value of data:");
			printDebugData();
		}

		private void printDebugData() {
			int numRows = getRowCount();
			int numCols = getColumnCount();

			for (int i = 0; i < numRows; i++) {
				log.info("    row " + i + ":");
				for (int j = 0; j < numCols; j++) {
					log.info("  " + data[i][j]);
				}
			}
		}
	}

	public SinglePartyTableModel getSinglePartyTableModel() {
		return singlePartyTableModel;
	}

	public void callStateChanged(PresentationModel model, int oldState,
			Call call) {
		// TODO Auto-generated method stub
		
	}

	public void channelAdded(PresentationModel model,
			ConferenceCall conferenceCall, Channel channel) {
		// TODO Auto-generated method stub
		
	}

	public void channelRemoved(PresentationModel model,
			ConferenceCall conferenceCall, Channel channel) {
		// TODO Auto-generated method stub
		
	}

	public void refreshTable(int callType) {
		// TODO Auto-generated method stub
		
	}
	
}
