package org.jmik.asterisk.model;

import java.util.List;
import java.util.Set;

import org.asteriskjava.manager.ManagerConnection;
import org.asteriskjava.manager.ManagerEventListener;
import org.jmik.asterisk.model.impl.Call;
import org.jmik.asterisk.model.impl.CallCostruction;

/**
 * This class represents an abstraction of telephony service provider software.
 * @author Michele La Porta
 *
 */
public interface Provider extends ManagerEventListener{

	// TODO public void record(Call call)
	
	/**
	 * Drop a call
	 * @param call
	 */
	public void drop(Call call);
	
	/**
	 * Monitor a call
	 * @param call
	 */
	public void monitor(Call call);

	/**
	 * Attach a call
	 * @param call
	 */
	public void attachCall(Call call);

	/**
	 * Detach a call
	 * @param call
	 */
	public void detachCall(Call call);

	/**
	 * Returns a set of attached calls of this Provider 
	 * @return Set
	 */
	public Set<Call> getAttachedCalls();

	public Set<CallCostruction> getCallConstrutions();

	public List<ProviderListener> getListeners();

	public ManagerConnection getManagerConnection();
	/**
	 * Remove a call construction
	 * @param callConstruction
	 */
	public void removeCallConstruction(CallCostruction callConstruction);
	
	/**
	 * Register a provider listener
	 * @param providerListener
	 */
	public void addListener(ProviderListener providerListener);
	
	/**
	 * Unregister a provider listener
	 * @param providerListener
	 */
	public void removeListener(ProviderListener providerListener);
	

}
