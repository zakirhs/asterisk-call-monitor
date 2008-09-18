package org.jmik.asterisk.model;

import org.jmik.asterisk.model.impl.Call;

/**
 * Interface to notify call attach/detach.
 * 
 * @author Michele La Porta
 *
 */
public interface ProviderListener{

	/**
	 * Notification of Call attached to Provider.
	 * @param call
	 */
	public void callAttached(Call call);
	
	/**
	 * Notification of Call detached to Provider.
	 * @param call
	 */
	public void callDetached(Call call);
	
}
