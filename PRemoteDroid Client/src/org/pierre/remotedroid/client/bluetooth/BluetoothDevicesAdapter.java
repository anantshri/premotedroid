package org.pierre.remotedroid.client.bluetooth;

import java.util.ArrayList;

import org.pierre.remotedroid.client.R;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BluetoothDevicesAdapter extends ArrayAdapter<BluetoothDevice>
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
	
	private static class BluetoothDevicesHolder
	{
		public TextView name;
		public TextView address;
	}
}
