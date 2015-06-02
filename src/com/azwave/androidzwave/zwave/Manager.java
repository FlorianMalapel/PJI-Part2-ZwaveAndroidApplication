package com.azwave.androidzwave.zwave;

//-----------------------------------------------------------------------------
//Copyright (c) 2010 Mal Lansell <openzwave@lansell.org>
//
//SOFTWARE NOTICE AND LICENSE
//
//This file is part of OpenZWave.
//
//OpenZWave is free software: you can redistribute it and/or modify
//it under the terms of the GNU Lesser General Public License as published
//by the Free Software Foundation, either version 3 of the License,
//or (at your option) any later version.
//
//OpenZWave is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//GNU Lesser General Public License for more details.
//
//You should have received a copy of the GNU Lesser General Public License
//along with OpenZWave.  If not, see <http://www.gnu.org/licenses/>.
//
//-----------------------------------------------------------------------------
//
//Ported to Java by: Peradnya Dinata <peradnya@gmail.com>
//
//-----------------------------------------------------------------------------

import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;

import com.azwave.androidzwave.R;
import com.azwave.androidzwave.zwave.driver.UsbSerialDriver;
import com.azwave.androidzwave.zwave.items.ControllerActionListener;
import com.azwave.androidzwave.zwave.items.QueueManager;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.nodes.NodeListener;
import com.azwave.androidzwave.zwave.nodes.NodeManager;
import com.azwave.androidzwave.zwave.nodes.Node.QueryStage;
import com.azwave.androidzwave.zwave.utils.Log;
import com.azwave.androidzwave.zwave.utils.XMLManager;
import com.azwave.androidzwave.zwave.utils.XMLManagerAndroid;

/**
 * Class which handle all the others.
 * Create the driver to allow the application to use the Zwave usb controller.
 * Create the NodeManager which manage all the Nodes found on the Zwave network.
 * @author florian.malapel@gmail.com
 */
public class Manager {

	private Driver zwaveDriver;
	private Log zwaveLog;
	private XMLManager zwaveXMLManager;
	private NodeManager zwaveNodeManager;
	private QueueManager zwavequeueManager;

	private Context context;
	private UsbSerialDriver serialDriver;

	/**
	 * Constructor which create the main objects of the application
	 * @param context : Context of the main activity
	 * @param serialDriver : The driver to create to control the Zwave usb controller
	 */
	public Manager(Context context, UsbSerialDriver serialDriver) {
		this.context = context;
		this.serialDriver = serialDriver;

		zwaveLog = new Log(context, R.layout.activity_main_listitem_log);
		zwaveXMLManager = new XMLManagerAndroid(context, zwaveLog);
		zwavequeueManager = new QueueManager(zwaveLog);
		zwaveNodeManager = new NodeManager(zwavequeueManager, zwaveXMLManager, zwaveLog);
		zwaveDriver = new Driver(zwavequeueManager, zwaveNodeManager, zwaveXMLManager, serialDriver, zwaveLog);
	}

	/**
	 * Set a listener on the NodeManager
	 * @param listener : the listener to set
	 */
	public void setNodeListener(NodeListener listener) {
		zwaveNodeManager.setListener(listener);
	}

	
	/**
	 * Start all the service needed to the Manager.
	 * @throws IOException
	 */
	public void open() throws IOException {
		zwaveLog.add("Start Android Z-Wave");
		zwaveLog.add("Initializing & Reading Z-Wave XML Data");
		zwaveXMLManager.readDeviceClasses();
		zwaveXMLManager.readManufacturerSpecific();
		zwaveLog.add("Driver is starting...");
		zwaveDriver.start();
	}

	/**
	 * Set a ControllerActionListener on the QueueManager
	 * @param listener : the listener to set
	 */
	public synchronized void setControllerActionListener(ControllerActionListener listener) {
		zwavequeueManager.setControllerActionListener(listener);
	}

	/** 
	 * @return True is all Nodes of the Zwave network are queried, else return False
	 */
	public boolean isAllNodesQueried() {
		return zwaveNodeManager.isAllNodesQueried();
	}

	/**
	 * Return a list of the Nodes alive on the Zwave network. 
	 * @return Arraylist<Node>
	 */
	public ArrayList<Node> getAllNodesAlive() {
		return zwaveNodeManager.toArrayListNodeAlive();
	}

	/**
	 * Return all the Nodes registred on the Zwave network
	 * @return ArrayList<Node>
	 */
	public ArrayList<Node> getAllNodes() {
		return zwaveNodeManager.toArrayList();
	}

	/**
	 * Count the number of Nodes alive on the network
	 * @return the number of Nodes alive
	 */
	public int nodesAliveCount() {
		return zwaveNodeManager.nodesAliveCount();
	}

	/**
	 * Count the number of Nodes registred on the network
	 * @return
	 */
	public int nodesCount() {
		return zwaveNodeManager.nodesCount();
	}

	/**
	 * Stop the driver for the Zwave usb controller
	 */
	public void close() {
		zwaveDriver.close();
	}

	public Log getLog() {
		return zwaveLog;
	}

	/**
	 * Update all of the Node registred on the Zwave network
	 */
	public void refreshValuesAllNodes(){
		for(Node n : getAllNodes()){
			n.setQueryStage(QueryStage.ProtocolInfo);
		}
	}
	
}
