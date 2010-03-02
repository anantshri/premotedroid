package org.pierre.remotedroid.client.activity;

import java.util.ArrayList;
import java.util.Set;

import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.client.bluetooth.BluetoothDevicesAdapter;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class BluetoothDevicesActivity extends ListActivity implements Runnable, OnItemClickListener
{
	private ArrayList<BluetoothDevice> bluetoothDevices;
	private BluetoothDevicesAdapter bluetoothDevicesAdapter;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.bluetoothDevices = new ArrayList<BluetoothDevice>();
		
		this.bluetoothDevicesAdapter = new BluetoothDevicesAdapter(this, R.layout.bluetoothdevice, this.bluetoothDevices);
		this.getListView().setAdapter(this.bluetoothDevicesAdapter);
		
		this.getListView().setOnItemClickListener(this);
	}
	
	protected void onResume()
	{
		super.onResume();
		
		(new Thread(this)).start();
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
	}
	
	public void run()
	{
		Looper.prepare();
		
		BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
		if (bluetoothAdapter != null)
		{
			if (bluetoothAdapter.isEnabled())
			{
				Set<BluetoothDevice> deviceSet = bluetoothAdapter.getBondedDevices();
				this.bluetoothDevices.addAll(deviceSet);
				
				this.runOnUiThread(new Runnable()
				{
					public void run()
					{
						BluetoothDevicesActivity.this.bluetoothDevicesAdapter.notifyDataSetChanged();
					}
				});
			}
		}
	}
}
