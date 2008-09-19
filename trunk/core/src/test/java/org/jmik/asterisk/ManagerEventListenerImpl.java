package org.jmik.asterisk;

import org.asteriskjava.manager.ManagerEventListener;
import org.asteriskjava.manager.event.ManagerEvent;

public class ManagerEventListenerImpl extends Thread implements ManagerEventListener{
	private boolean runner ;
	
	public void onManagerEvent(ManagerEvent managerEvent) {
		if(!runner){
			this.start();
			runner = true;
		}
		System.out.println(managerEvent);
		
	}

	public void run() {
		while(runner){
			try {
				Thread.sleep(1000);
				System.out.println("run()");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

}
