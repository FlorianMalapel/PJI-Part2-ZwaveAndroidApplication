package com.azwave.androidzwave.zwave.nodes;

import java.util.EventListener;

/**
 * Represent a Listener for a Node
 */
public interface NodeListener extends EventListener {

	public void onNodeAliveListener(boolean alive);

	public void onNodeQueryStageCompleteListener();

	public void onNodeAddedToList();

	public void onNodeRemovedToList();

}
