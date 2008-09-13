package org.mik.asterisk.model;

import org.mik.asterisk.model.call.Call;

public interface ProviderListener {
	public void callAttached(Call call);
	public void callDetached(Call call);
}
