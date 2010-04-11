package org.pierre.remotedroid.protocol.bluetooth;

import java.io.IOException;
import java.util.UUID;

import org.pierre.remotedroid.client.app.PRemoteDroid;
import org.pierre.remotedroid.protocol.PRemoteDroidConnection;

import android.content.Intent;
import android.os.Looper;
import backport.android.bluetooth.BluetoothAdapter;
import backport.android.bluetooth.BluetoothDevice;
import backport.android.bluetooth.BluetoothSocket;

public class PRemoteDroidConnectionBluetoothBackport extends PRemoteDroidConnection
{
	private BluetoothSocket socket;
	
	public PRemoteDroidConnectionBluetoothBackport(BluetoothSocket socket) throws IOException
	{
		super(socket.getInputStream(), socket.getOutputStream());
		
		this.socket = socket;
	}
	
	public static PRemoteDroidConnectionBluetoothBackport create(PRemoteDroid application, String address) throws IOException
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
						BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString(PRemoteDroidConnection.BLUETOOTH_UUID));
						socket.connect();
						
						PRemoteDroidConnectionBluetoothBackport connection = new PRemoteDroidConnectionBluetoothBackport(socket);
						
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
