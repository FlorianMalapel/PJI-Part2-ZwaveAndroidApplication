package com.azwave.androidzwave;

import java.util.ArrayList;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.Toast;

import com.azwave.androidzwave.module.NodeGridAdapter;
import com.azwave.androidzwave.zwave.Manager;
import com.azwave.androidzwave.zwave.driver.UsbSerialDriver;
import com.azwave.androidzwave.zwave.driver.UsbSerialProber;
import com.azwave.androidzwave.zwave.items.ControllerActionListener;
import com.azwave.androidzwave.zwave.items.ControllerCmd.ControllerError;
import com.azwave.androidzwave.zwave.items.ControllerCmd.ControllerState;
import com.azwave.androidzwave.zwave.nodes.Node;
import com.azwave.androidzwave.zwave.nodes.NodeListener;

/**
 * This activity is the default activity open by android, which defines the layout of the application,
 * creates the objects and objects listener necessary to set up the Zwave network.
 * Implements NodeListener and ControllerActionListener are two EventListener which allow the application
 * to do actions when node are added, removed, or all added to the list.
 * @author florian.malapel@gmail.com
 */
public class MainActivity extends Activity implements NodeListener, ControllerActionListener {

	// --- View 
	private GridView zwaveNodeGrid;			// Contains and display all the Nodes found
	private NodeGridAdapter nodeGridAdapter;// Adapter to manage zwaveNodeGrid
	private ListViewUpdate listViewUpdate;
	private Button refresh;
	// --------
	
	// --- Network & usb management
	private UsbManager usbManager;
	private UsbSerialDriver serialDriver;
	private Manager zwaveManager;
	private volatile long initStartTime = 0;
	private volatile long initEndTime = 0;	
	private Boolean finish = false;
	// Allow to have permissions to access to the usb key from the android device
	private static final String ACTION_USB_PERMISSION = "com.azwave.androidzwave.USB_PERMISSION";
	private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
	    public void onReceive(Context context, Intent intent) {
	        String action = intent.getAction();
	        if (ACTION_USB_PERMISSION.equals(action)) {
	            synchronized (this) {
	                UsbDevice device = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
	                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
	                    if(device != null){
	                    	Log.d("DEBUG", "permission accepted for device " + device);
	                   }
	                } 
	                else {
	                    Log.d("DEBUG", "permission denied for device " + device);
	                }
	            }
	        }
	    }
	};
	// ------------------------
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// Set up the layout of this activity
		setContentView(R.layout.main_activity);
		zwaveNodeGrid = (GridView) findViewById(R.id.gridview);
		refresh = (Button) findViewById(R.id.refresh);
		initUsbDriver();
		refresh.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				zwaveManager.refreshValuesAllNodes();
			}
		});
	}

	@Override
	public void finish() {
		if (serialDriver != null) {
			try {
				serialDriver.close();
				zwaveManager.close();
				listViewUpdate.close();
			} catch (Exception x) {
				Log.e("DEBUG", x.getMessage());
			}
		}
		unregisterReceiver(mUsbReceiver);
		super.finish();
	}

	/**
	 * Initialize the driver to use the Zwave usb controller, and the Manager which will
	 * create objects to allow the application to get all the Nodes registred on the Zwave 
	 * network
	 */
	private void initUsbDriver() {
		PendingIntent mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
		IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
		registerReceiver(mUsbReceiver, filter);
		usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
		serialDriver = UsbSerialProber.acquire(usbManager, mPermissionIntent);
		try {
			zwaveManager = new Manager(this, serialDriver);			
			listViewUpdate = new ListViewUpdate();
			listViewUpdate.execute(zwaveManager);
			zwaveManager.setNodeListener(this);
			zwaveManager.setControllerActionListener(this);
			serialDriver.open();
			initStartTime = System.currentTimeMillis();
			zwaveManager.open();
			nodeGridAdapter = new NodeGridAdapter(this, R.layout.main_grid_item_node);
			nodeGridAdapter.setNotifyOnChange(true);
			zwaveNodeGrid.setAdapter(nodeGridAdapter);
		} catch (Exception x) {
			finish();
		}
	}
	
	/**
	 * Internal class extending an AsyncTask, which allow to get an ArrayList of Node alive via 
	 * the Manager and keep it upgraded.
	 * @author florian.malapel@gmail.com
	 */
	private class ListViewUpdate extends AsyncTask<Manager, ArrayList<Node>, Void> {
		public volatile boolean lock = true;
		public volatile int size = 0;
		public volatile boolean foundupdate = false;
		
		/**
		 * Get all the Nodes alive from the manager, and copy it in an ArrayList.
		 * Then invoke publishProgress with the ArrayList to signal the AsynTask that
		 * updates has been made. 
		 */
		@Override
		protected Void doInBackground(Manager... arg0) {
			ArrayList<Node> nodes = null;
			while (lock) {
				if (foundupdate) {
					nodes = arg0[0].getAllNodesAlive();
					publishProgress(nodes);
					if (foundupdate) {
						foundupdate = false;
					}
				}
			}
			return null;
		}

		/**
		 * Invoke after the method publishProgress(ArrayList<Nodes) has been called.
		 * Clear the adapter (of the GridView) and add all the new Nodes of the ArrayList
		 * in parameter.
		 */
		@Override
		protected void onProgressUpdate(ArrayList<Node>... progress) {
			if (progress[0] != null) {
				nodeGridAdapter.clear();
				for (int i = 0; i < progress[0].size(); i++) {
					nodeGridAdapter.add(progress[0].get(i));
				}
				nodeGridAdapter.notifyDataSetChanged();
			}
		}
		public synchronized void close() {
			lock = false;
		}
	}

	@Override
	public void onNodeAliveListener(boolean alive) {
		listViewUpdate.foundupdate = true;
	}

	@Override
	public void onNodeQueryStageCompleteListener() {
		listViewUpdate.foundupdate = true;
		final Activity nowActivity = this;
		if (zwaveManager != null && zwaveManager.isAllNodesQueried()) {
			new Thread(new Runnable() {
				@Override
				public void run() {
					nowActivity.runOnUiThread(new Runnable() {
						@Override
						public void run() {
							initEndTime = System.currentTimeMillis();
							if(!finish){
								finish = true;
								zwaveManager.refreshValuesAllNodes();
							}
						}
					});
				}
			}).run();
		}
	}

	@Override
	public void onNodeAddedToList() {
	}

	@Override
	public void onNodeRemovedToList() {
		listViewUpdate.foundupdate = true;
	}

	@Override
	public void onAction(final ControllerState state, ControllerError error, Object context) {
		final Activity nowActivity = this;
		new Thread(new Runnable() {
			public void run() {
				nowActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						switch (state) {
						case Normal:
							break;
						case Starting:
							break;
						case Cancel:
							break;
						case Error:
							break;
						case Waiting:
							Toast.makeText(nowActivity, "Waiting for node initiator ...", Toast.LENGTH_LONG).show();
							break;
						case Sleeping:
							break;
						case InProgress:
							Toast.makeText(nowActivity, "Plase wait ...", Toast.LENGTH_LONG).show();
							break;
						case Completed:
							Toast.makeText(nowActivity, "Controller command complete ...", Toast.LENGTH_LONG).show();
							break;
						case Failed:
							Toast.makeText(nowActivity, "Controller command failed ...", Toast.LENGTH_LONG).show();
							break;
						case NodeOK:
							break;
						case NodeFailed:
							break;
						}
					}
				});
			}
		}).run();
	}
}
