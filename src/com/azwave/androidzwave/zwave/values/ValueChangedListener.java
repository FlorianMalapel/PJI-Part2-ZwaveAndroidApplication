package com.azwave.androidzwave.zwave.values;

import java.util.EventListener;

/**
 * Listener of a Value.
 */
public interface ValueChangedListener extends EventListener {

	public void onValueChanged(Value value);

}
