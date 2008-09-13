package org.mik.asterisk.model.constant;

import java.util.ResourceBundle;

/**
 * @author Michele La Porta
 *
 */
public class Constants {

	static final String CONFIGURATION_BUNDLE = "callwatcher";

	private static ResourceBundle resourceBundle;

	static{
		resourceBundle = ResourceBundle.getBundle(CONFIGURATION_BUNDLE);
	}

	public static String asteriskIpAddress = resourceBundle.getString("callmonitor.asterisk.ip");
	public static int asteriskPort = Integer.parseInt(resourceBundle.getString("callmonitor.asterisk.port"));

	public static String asteriskManagerUser = resourceBundle.getString("callmonitor.asterisk.manager.user");
	public static String asteriskManagerPassword = resourceBundle.getString("callmonitor.asterisk.manager.password");

	public static String conferenceMonitorIpAddress = resourceBundle.getString("callmonitor.conference.ip");
	public static int conferenceMonitorPort = Integer.parseInt(resourceBundle.getString("callmonitor.conference.port"));
	

}
