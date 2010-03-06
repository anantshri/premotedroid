package org.pierre.remotedroid.client.activity;

import java.util.ArrayList;
import java.util.Set;

import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.client.app.PRemoteDroid;
import org.pierre.remotedroid.client.bluetooth.BluetoothDevicesAdapter;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class BluetoothDevicesActivity extends ListActivity implements Runnable, OnItemClickListener
{
	private PRemoteDroid application;
	
	private ArrayList<BluetoothDevice> bluetoothDevices;
	private BluetoothDevicesAdapter bluetoothDevicesAdapter;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.application = (PRemoteDroid) this.getApplication();
		
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
		BluetoothDevice bluetoothDevice = this.bluetoothDevices.get(position);
		String address = bluetoothDevice.getAddress();
		
		Intent intent = new Intent();
		intent.putExtra("address", address);
		
		this.setResult(RESULT_OK, intent);
		
		this.finish();
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
			else
			{
				this.application.showToast(R.string.text_bluetooth_not_enabled);
			}
		}
		else
		{
			this.application.showToast(R.string.text_bluetooth_not_available);
		}
	}
}
