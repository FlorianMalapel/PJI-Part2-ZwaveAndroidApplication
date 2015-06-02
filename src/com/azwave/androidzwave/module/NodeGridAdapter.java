/**
 * 
 */
package com.azwave.androidzwave.module;

import java.util.Iterator;
import java.util.Map.Entry;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Switch;
import android.widget.TextView;

import com.azwave.androidzwave.R;
import com.azwave.androidzwave.zwave.commandclass.Alarm;
import com.azwave.androidzwave.zwave.commandclass.Basic;
import com.azwave.androidzwave.zwave.commandclass.CommandClass;
import com.azwave.androidzwave.zwave.commandclass.Meter;
import com.azwave.androidzwave.zwave.commandclass.MeterPulse;
import com.azwave.androidzwave.zwave.commandclass.PowerLevel;
import com.azwave.androidzwave.zwave.commandclass.SwitchAll;
import com.azwave.androidzwave.zwave.commandclass.SwitchBinary;
import com.azwave.androidzwave.zwave.commandclass.SwitchMultiLevel;
import com.azwave.androidzwave.zwave.items.ControllerCmd.ControllerCommand;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.utils.SafeCast;
import com.azwave.androidzwave.zwave.values.Value;
import com.azwave.androidzwave.zwave.values.ValueBool;
import com.azwave.androidzwave.zwave.values.ValueByte;
import com.azwave.androidzwave.zwave.values.ValueChangedListener;
import com.azwave.androidzwave.zwave.values.ValueDecimal;
import com.azwave.androidzwave.zwave.values.ValueId;
import com.azwave.androidzwave.zwave.values.ValueId.ValueGenre;
import com.azwave.androidzwave.zwave.values.ValueInt;
import com.azwave.androidzwave.zwave.values.ValueManager;
import com.azwave.androidzwave.zwave.values.ValueShort;
import com.azwave.androidzwave.zwave.values.ValueString;

/**
 * Adapter of the GridView which display all the Nodes registered on the Zwave network.
 * For each Node, the adapter find its functions and display android components depending
 * upon this functions to allow users to do actions on this Node. 
 * @author florian.malapel@gmail.com
 */
public class NodeGridAdapter extends ArrayAdapter<Node> implements ValueChangedListener {

	private Context mContext;
	private int listItemRessourceId;
	
	/**
	 * Constructor of the Adpater
	 * @param _mContext is the Context of the activity which call this adapter
	 * @param textViewResourceId is the layout which describe a single Node.
	 */
	public NodeGridAdapter(Context _mContext, int textViewResourceId) {
		super(_mContext, textViewResourceId);
		mContext = _mContext;
		listItemRessourceId = textViewResourceId;
	}
	
	
	/**
	 * Create a new component for each item referenced by the Adapter, find its functions
	 * and display components which allow to control it. Create also a list of informations
	 * sent by the Node.
	 */
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(listItemRessourceId, parent, false);

		final Node node = getItem(position);
		final ValueManager valuesManager = getItem(position).getValueManager();
		final TextView nodeName = (TextView) rowView.findViewById(R.id.node_name);
		nodeName.setText(String.format("Node %d -- %s", SafeCast.toInt(node.getNodeId()), node.getProductName()));

		final Switch nodeSwitch = (Switch) rowView.findViewById(R.id.node_switch);
		final SeekBar nodeSeekbar = (SeekBar) rowView.findViewById(R.id.node_seek);
		final LinearLayout nodeController = (LinearLayout) rowView.findViewById(R.id.node_controller);
		final Button nodeAdd = (Button) rowView.findViewById(R.id.node_add_controller);
		final Button nodeRemove = (Button) rowView.findViewById(R.id.node_remove_controller);
		final LinearLayout listValues = (LinearLayout) rowView.findViewById(R.id.listValues);
		
		/* First, all the components are hidden because not all Nodes need them */
		nodeSwitch.setVisibility(View.GONE);
		nodeSeekbar.setVisibility(View.GONE);
		nodeAdd.setVisibility(View.GONE);
		nodeRemove.setVisibility(View.GONE);
		
		node.getCommandClassManager().displayCommandClasses();
		
		// If the node is a controller
		if (node.isController()) {
			nodeAdd.setVisibility(View.VISIBLE);
			nodeRemove.setVisibility(View.VISIBLE);
			nodeAdd.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					node.getQueueManager().sendControllerCommand(ControllerCommand.AddDevice, false, node.getNodeId());
				}
			});

			nodeRemove.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					node.getQueueManager().sendControllerCommand(ControllerCommand.RemoveDevice, false, node.getNodeId());
				}
			});

		}
		// Else if the node get the Basic commandClass and is not a controller
		else if(node.getCommandClassManager().getCommandClass(Basic.COMMAND_CLASS_ID) != null && !node.isController()){
			final ValueByte value = (ValueByte) node.getValueManager().getValue(Basic.COMMAND_CLASS_ID, (byte) 1, (byte) 0);
			value.setValueChangedListener(this);
			
			// If the node have the switchbinary function
			if(node.getCommandClassManager().getCommandClass(SwitchBinary.COMMAND_CLASS_ID) != null){
				nodeSwitch.setVisibility(View.VISIBLE);
				nodeSwitch.setChecked(value.getValue() == 0 ? false : true);
				nodeSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						if (isChecked) {
							node.setOn();
							Basic cc = (Basic) node.getCommandClassManager().getCommandClass(Basic.COMMAND_CLASS_ID);
							cc.set((byte)3);
						} else {
							node.setOff();
						}
					}
				});
			}
			
			// If the node is a SwitchMultiLevel
			if(node.getCommandClassManager().getCommandClass(SwitchMultiLevel.COMMAND_CLASS_ID) != null){
				final ValueByte valueSwitch = (ValueByte) node.getValueManager()
						.getValue(Basic.COMMAND_CLASS_ID, (byte) 1, (byte) 0);
				value.setValueChangedListener(this);

				int progress = 0;
				final int values = SafeCast.toInt(valueSwitch.getValue());
				if (values == 255 || values == 99) {
					progress = 5;
				} else if (values >= 0 && values < 99) {
					progress = values / 20;
				} else {
					progress = 5;
				}

				nodeSeekbar.setProgress(progress);
				nodeSeekbar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
					private byte level = 0;
					@Override
					public void onProgressChanged(SeekBar arg0, int arg1,
							boolean arg2) {
						// TODO Auto-generated method stub
						level = (byte) ((arg1 * 20) >= 100 ? 99
								: (arg1 * 20));
					}
					@Override
					public void onStartTrackingTouch(SeekBar arg0) {
						// TODO Auto-generated method stub
					}
					@Override
					public void onStopTrackingTouch(SeekBar arg0) {
						// TODO Auto-generated method stub
						node.setLevel(level);
					}
				});
 			}
		}
		createListValues(listValues, valuesManager);
		return rowView;
	}

	@Override
	public void onValueChanged(Value value) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				final Activity y = (Activity) getContext();
				y.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						notifyDataSetChanged();
					}
				});
			}
		}).start();
	}
	
	/**
	 * Create a list which will display the values sent by a node, before display them, the method
	 * find the type of the value, get the Label, unit and value, and after add it to the LinearLayout
	 * @param listValues is the LinearLayout where the values of the node will be displayed
	 * @param manager is the ValueManager which contains all the values of a node
	 */
	public synchronized void createListValues(LinearLayout listValues, ValueManager manager){
		Iterator it = manager.entrySet().iterator();
		while(it.hasNext()){
			Entry<Integer, Value> val = (Entry<Integer, Value>)it.next();
			Value tmp = val.getValue();
			if(tmp.getId().getGenre() == ValueId.ValueGenre.USER){
				tmp.setValueChangedListener(this);
				TextView value = new TextView(mContext);
				TextView label = new TextView(mContext);
				TextView unit = new TextView(mContext);
				// Find the type of the Value
				switch (tmp.getId().getType()) {
					case BOOL:
						value.setText(Boolean.toString( ((ValueBool)tmp).getValue() ));
						label.setText(((ValueBool)tmp).getLabel());
						unit.setText(((ValueBool)tmp).getUnits());
						break;
					case BYTE:
						value.setText(Byte.toString( ((ValueByte)tmp).getValue() ));
						label.setText(((ValueByte)tmp).getLabel());
						unit.setText(((ValueByte)tmp).getUnits());
						break;
					case DECIMAL:
						value.setText(((ValueDecimal)tmp).getValue());
						label.setText(((ValueDecimal)tmp).getLabel());
						unit.setText(((ValueDecimal)tmp).getUnits());
						break;
					case INT:
						value.setText(Integer.toString(((ValueInt)tmp).getValue()));
						label.setText(((ValueInt)tmp).getLabel());
						unit.setText(((ValueInt)tmp).getUnits());
						break;
					case LIST:
						
						break;
					case SCHEDULE:
						
						break;
					case SHORT:
						value.setText(Short.toString(((ValueShort)tmp).getValue()));
						label.setText(((ValueShort)tmp).getLabel());
						unit.setText(((ValueShort)tmp).getUnits());
						break;
					case STRING:
						value.setText(((ValueString)tmp).getValue());
						label.setText(((ValueString)tmp).getLabel());
						unit.setText(((ValueString)tmp).getUnits());
						break;
					case BUTTON:
						
						break;
					case RAW:
						
						break;
		
					default:
						break;
				}
				// Add the value, label and unit to a LinearLayout to display them nicely
				String adapt = value.getText().toString().replaceFirst("00000", "");
				value.setText(adapt);
				value.setTextColor(Color.BLACK);
				unit.setTextColor(Color.BLACK);
				label.setTextColor(Color.BLACK);
				LinearLayout container = new LinearLayout(mContext);
				container.setOrientation(LinearLayout.HORIZONTAL);
				LinearLayout.LayoutParams containerParam = new LinearLayout.LayoutParams
						(LayoutParams.MATCH_PARENT, 0, 1.0f);
				containerParam.setMargins(0, 2, 0, 0);
				container.setBackgroundColor(Color.rgb(210,210,210));
				LinearLayout.LayoutParams param = new LinearLayout.LayoutParams
						(0, LayoutParams.MATCH_PARENT, 1.0f);
				container.setLayoutParams(containerParam);
				value.setLayoutParams(param);
				label.setLayoutParams(param);
				unit.setLayoutParams(param);
				container.addView(label);
				container.addView(value);
				container.addView(unit);
				listValues.addView(container);
			}
		}
	}

}
