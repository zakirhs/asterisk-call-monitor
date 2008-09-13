package org.jmik.asterisk.gui;

import org.asteriskjava.fastagi.DefaultAgiServer;


public class HelloAgiTest{
	
	public static void main(final String[] args) {
		try {
			DefaultAgiServer defaultAgiServer = new DefaultAgiServer();
			defaultAgiServer.startup();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
