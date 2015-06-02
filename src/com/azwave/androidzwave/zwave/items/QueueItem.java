package com.azwave.androidzwave.zwave.items;

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

import com.azwave.androidzwave.zwave.Defs;
import com.azwave.androidzwave.zwave.nodes.Node.QueryStage;

/**
 * Represent an Item of the PriorityBlockingQueue in the class QueueManager.
 * Contains all this informations necessary to send a Msg.
 */
public class QueueItem {

	public enum QueueCommand {
		SendMessage, QueryStageComplete, Controller
	}

	public enum QueuePriority {
		Command, NoOperation, Controller, WakeUp, Send, Query, Poll
	}

	private QueueCommand command = QueueCommand.SendMessage;
	private QueuePriority priority = QueuePriority.Send;
	private Msg message = null;
	private ControllerCmd cci = null;
	private byte nodeId = Defs.NODE_BROADCAST;
	private QueryStage queryStage = QueryStage.None;
	private boolean retry = false;
	private long queueCount = 0;

	public QueueItem() {
	}

	/**
	 * Create a QueueItem with a Msg, and get the Node id of the Msg
	 * 
	 * @param msg
	 *            : a Msg message to sent
	 */
	public QueueItem(Msg msg) {
		message = msg;
		nodeId = message.getNodeId();
	}

	/**
	 * Return the type of the Command.
	 * 
	 * @return a QueueCommand (SendMessage | QueryStageComplete | Controller)
	 */
	public QueueCommand getCommand() {
		return command;
	}

	/**
	 * Set the type of the QueueItem.
	 * 
	 * @param command
	 *            : QueueCommand (SendMessage | QueryStageComplete | Controller)
	 */
	public void setCommand(QueueCommand command) {
		this.command = command;
	}

	/**
	 * Get the prority of the QueueItem.
	 * 
	 * @return a QueuePriority (Command | NoOperation | Controller | WakeUp |
	 *         Send | Query | Poll)
	 */
	public QueuePriority getPriority() {
		return priority;
	}

	/**
	 * Set the priority of the QueueItem (Command | NoOperation | Controller |
	 * WakeUp | Send | Query | Poll)
	 * 
	 * @param priority
	 */
	public void setPriority(QueuePriority priority) {
		this.priority = priority;
	}

	/**
	 * Get the Msg contained by the QueueItem
	 * @return a Msg
	 */
	public Msg getMsg() {
		return message;
	}

	/**
	 * Set the Msg of the QueueItem
	 * @param Msg message 
	 */
	public void setMsg(Msg message) {
		this.message = message;
	}

	public ControllerCmd getCci() {
		return cci;
	}

	public void setCci(ControllerCmd cci) {
		this.cci = cci;
	}

	public byte getNodeId() {
		return nodeId;
	}

	public void setNodeId(byte nodeId) {
		this.nodeId = nodeId;
	}

	public QueryStage getQueryStage() {
		return queryStage;
	}

	public void setQueryStage(QueryStage queryStage) {
		this.queryStage = queryStage;
	}

	public boolean isRetry() {
		return retry;
	}

	public void setRetry(boolean retry) {
		this.retry = retry;
	}

	public long getQueueCount() {
		return queueCount;
	}

	public void setQueueCount(long queueCount) {
		this.queueCount = queueCount;
	}

}
