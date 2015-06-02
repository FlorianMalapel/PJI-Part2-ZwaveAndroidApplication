package com.azwave.androidzwave.zwave.commandclass;

import com.azwave.androidzwave.zwave.items.Msg;
import com.azwave.androidzwave.zwave.items.QueueItem.QueuePriority;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.utils.SafeCast;
import com.azwave.androidzwave.zwave.values.Value;
import com.azwave.androidzwave.zwave.values.ValueInt;

public class Meter extends CommandClass{
	
	public static final byte COMMAND_CLASS_ID = (byte) 0x32;
	public static final String COMMAND_CLASS_NAME = "COMMAND_CLASS_METER";
	
	public static final byte METER_CMD_GET = (byte) 0x03;
	public static final byte METER_CMD_REPORT = (byte) 0x05;

	public Meter(Node node) {
		super(node);
	}
	
	public boolean requestState(int requestFlags, byte instance, QueuePriority queue) {
		if ((requestFlags & REQUEST_FLAG_DYNAMIC) != 0) {
			return requestValue(requestFlags, (byte) 0, instance, queue);
		}
		return false;
	}
	
	public boolean requestValue(int requestFlags, byte dummy, byte instance, QueuePriority queue) {
		Msg msg = Msg.createZWaveApplicationCommandHandler(getNodeId(), COMMAND_CLASS_ID);
		msg.setInstance(this, instance);
		msg.appends(new byte[] { getNodeId(), 2, COMMAND_CLASS_ID, METER_CMD_GET, node.getQueueManager().getTransmitOptions() });
		node.getQueueManager().sendMsg(msg, queue);
		return true;
	}

	@Override
	public byte getCommandClassId() {
		return COMMAND_CLASS_ID;
	}

	@Override
	public String getCommandClassName() {
		return COMMAND_CLASS_NAME;
	}

	@Override
	public byte getMaxVersion() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean handleMsg(byte[] data, int length, byte instance) {
		if (METER_CMD_REPORT == data[0]) {
			int count = 0;
			for (int i = 0; i < 4; i++) {
				count <<= 8;
				count |= SafeCast.toInt(data[i + 1]);
			}

			ValueInt value = (ValueInt) getValue(instance, (byte) 0);
			if (value != null) {
				value.onValueRefreshed(count);
			}
			return true;
		}
		return false;
	}

	@Override
	public void setValueBasic(byte instance, byte level) {
	}

	@Override
	public void createVars(byte mInstance) {		
	}

	@Override
	public void createVars(byte mInstance, byte index) {	
	}

	@Override
	public boolean setValue(Value value) {
		return false;
	}}
