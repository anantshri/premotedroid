package com.ControllerDroid.protocol.bluetooth;

import java.io.IOException;
import java.util.UUID;

import com.ControllerDroid.client.app.ControllerDroid;
import com.ControllerDroid.protocol.ControllerDroidConnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Looper;

public class ControllerDroidConnectionBluetooth extends ControllerDroidConnection
{
	private BluetoothSocket socket;
	
	public ControllerDroidConnectionBluetooth(BluetoothSocket socket) throws IOException
	{
		super(socket.getInputStream(), socket.getOutputStream());
		
		this.socket = socket;
	}
	
	public static ControllerDroidConnectionBluetooth create(ControllerDroid application, String address) throws IOException
	{
		Looper.prepare();
		
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		
		if (adapter != null)
		{
			if (adapter.isEnabled())
			{
				try
				{
					BluetoothDevice device = adapter.getRemoteDevice(address);
					
					if (device != null)
					{
						BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString(ControllerDroidConnection.BLUETOOTH_UUID));
						socket.connect();
						
						ControllerDroidConnectionBluetooth connection = new ControllerDroidConnectionBluetooth(socket);
						
						return connection;
					}
				}
				catch (IllegalArgumentException e)
				{
					throw new IOException();
				}
			}
			else
			{
				if (application.requestEnableBluetooth())
				{
					Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
					intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					application.startActivity(intent);
				}
			}
		}
		
		throw new IOException();
	}
	
	public void close() throws IOException
	{
		this.socket.close();
		super.close();
	}
}
