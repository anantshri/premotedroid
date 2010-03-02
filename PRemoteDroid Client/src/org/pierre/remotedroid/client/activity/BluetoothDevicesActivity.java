package org.pierre.remotedroid.client.activity;

import java.util.ArrayList;
import java.util.Set;

import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.os.Looper;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class BluetoothDevicesActivity extends ListActivity implements Runnable, OnItemClickListener
{
	private ArrayList<BluetoothDevice> devices;
	private ArrayAdapter<BluetoothDevice> adapter;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.devices = new ArrayList<BluetoothDevice>();
		
		this.adapter = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1, this.devices);
		this.getListView().setAdapter(this.adapter);
		
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
		
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		
		if (adapter != null)
		{
			if (adapter.isEnabled())
			{
				Set<BluetoothDevice> deviceSet = adapter.getBondedDevices();
				this.devices.addAll(deviceSet);
				
				this.runOnUiThread(new Runnable()
				{
					public void run()
					{
						BluetoothDevicesActivity.this.adapter.notifyDataSetChanged();
					}
				});
			}
		}
	}
}
