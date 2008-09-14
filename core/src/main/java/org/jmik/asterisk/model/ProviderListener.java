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
	 * 
	 * @param call
	 */
	public void callAttached(Call call);
	
	/**
	 * 
	 * @param call
	 */
	public void callDetached(Call call);
	
}
