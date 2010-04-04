package org.pierre.remotedroid.client.activity.connection;

import java.util.ArrayList;
import java.util.Set;

import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.client.app.PRemoteDroid;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class BluetoothDevicesActivity extends ListActivity implements OnItemClickListener
{
	public static final int ENABLE_BLUETOOTH_REQUEST_CODE = 0;
	
	private PRemoteDroid application;
	
	private ArrayList<BluetoothDevice> deviceList;
	private BluetoothDevicesAdapter deviceListAdapter;
	
	private BluetoothDevicesBroadcastReceiver broadcastReceiver;
	
	private BluetoothAdapter bluetoothAdapter;
	
	private boolean requestEnableBluetooth;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.application = (PRemoteDroid) this.getApplication();
		
		this.deviceList = new ArrayList<BluetoothDevice>();
		
		this.deviceListAdapter = new BluetoothDevicesAdapter(this, R.layout.bluetoothdevice, this.deviceList);
		this.getListView().setAdapter(this.deviceListAdapter);
		
		this.broadcastReceiver = new BluetoothDevicesBroadcastReceiver();
		
		this.requestEnableBluetooth = true;
		
		this.getListView().setOnItemClickListener(this);
	}
	
	protected void onResume()
	{
		super.onResume();
		
		this.registerReceiver(this.broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_STARTED));
		this.registerReceiver(this.broadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED));
		this.registerReceiver(this.broadcastReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
		
		this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		if (this.bluetoothAdapter != null)
		{
			if (this.bluetoothAdapter.isEnabled())
			{
				Set<BluetoothDevice> deviceSet = this.bluetoothAdapter.getBondedDevices();
				this.deviceList.addAll(deviceSet);
				this.deviceListAdapter.notifyDataSetChanged();
				
				this.bluetoothAdapter.startDiscovery();
			}
			else
			{
				if (this.requestEnableBluetooth)
				{
					this.startActivity(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE));
					
					this.requestEnableBluetooth = false;
				}
				else
				{
					this.finish();
				}
			}
		}
		else
		{
			this.application.showToast(R.string.text_bluetooth_not_available);
		}
	}
	
	protected void onPause()
	{
		super.onPause();
		
		if (this.bluetoothAdapter != null && bluetoothAdapter.isDiscovering())
		{
			this.bluetoothAdapter.cancelDiscovery();
		}
		
		this.unregisterReceiver(this.broadcastReceiver);
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		BluetoothDevice bluetoothDevice = this.deviceList.get(position);
		
		if (bluetoothDevice.getBondState() != BluetoothDevice.BOND_BONDED)
		{
			this.application.showToast(R.string.text_bluetooth_device_not_paired);
		}
		
		String address = bluetoothDevice.getAddress();
		
		Intent intent = new Intent();
		intent.putExtra("address", address);
		
		this.setResult(RESULT_OK, intent);
		
		this.finish();
	}
	
	private class BluetoothDevicesAdapter extends ArrayAdapter<BluetoothDevice>
	{
		private ArrayList<BluetoothDevice> bluetoothDevices;
		private LayoutInflater layoutInflater;
		
		public BluetoothDevicesAdapter(Context context, int textViewResourceId, ArrayList<BluetoothDevice> bluetoothDevices)
		{
			super(context, textViewResourceId, bluetoothDevices);
			
			this.bluetoothDevices = bluetoothDevices;
			this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		public View getView(int position, View convertView, ViewGroup parent)
		{
			BluetoothDevicesHolder holder;
			
			if (convertView == null)
			{
				convertView = this.layoutInflater.inflate(R.layout.bluetoothdevice, null);
				
				holder = new BluetoothDevicesHolder();
				holder.name = (TextView) convertView.findViewById(R.id.name);
				holder.address = (TextView) convertView.findViewById(R.id.address);
				
				convertView.setTag(holder);
			}
			else
			{
				holder = (BluetoothDevicesHolder) convertView.getTag();
			}
			
			BluetoothDevice bluetoothDevice = this.bluetoothDevices.get(position);
			holder.name.setText(bluetoothDevice.getName());
			holder.address.setText(bluetoothDevice.getAddress());
			
			return convertView;
		}
		
		private class BluetoothDevicesHolder
		{
			public TextView name;
			public TextView address;
		}
	}
	
	private class BluetoothDevicesBroadcastReceiver extends BroadcastReceiver
	{
		public void onReceive(Context context, Intent intent)
		{
			String action = intent.getAction();
			
			if (action.equals(BluetoothDevice.ACTION_FOUND))
			{
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				
				if (!BluetoothDevicesActivity.this.deviceList.contains(device))
				{
					BluetoothDevicesActivity.this.deviceList.add(device);
					BluetoothDevicesActivity.this.deviceListAdapter.notifyDataSetChanged();
				}
			}
			else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_STARTED))
			{
				BluetoothDevicesActivity.this.application.showToast(R.string.text_bluetooth_discovery_started);
			}
			else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
			{
				BluetoothDevicesActivity.this.application.showToast(R.string.text_bluetooth_discovery_finished);
			}
		}
	}
}
