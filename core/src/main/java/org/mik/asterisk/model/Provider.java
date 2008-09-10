package org.mik.asterisk.model;

import java.util.Set;

import org.mik.asterisk.model.call.Call;
import org.mik.asterisk.model.call.CallConstruction;

public interface Provider {
	public void attachCall(Call call);
	public void drop(Call call);
	public void monitor(Call call);
	
	public void removeCallConstruction(CallConstruction callConstruction);
	public Set getAttachedCalls();
	
	public void addListener(ProviderListener listener);
	public void removeListener(ProviderListener listener);
}