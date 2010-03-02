package org.pierre.remotedroid.protocol.bluetooth;

import java.io.IOException;
import java.util.UUID;

import org.pierre.remotedroid.protocol.PRemoteDroidConnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

public class PRemoteDroidConnectionBluetooth extends PRemoteDroidConnection
{
	private BluetoothSocket socket;
	
	public PRemoteDroidConnectionBluetooth(BluetoothSocket socket) throws IOException
	{
		super(socket.getInputStream(), socket.getOutputStream());
		
		this.socket = socket;
	}
	
	public static PRemoteDroidConnectionBluetooth create(String address) throws IOException
	{
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		
		if (adapter != null)
		{
			if (adapter.isEnabled())
			{
				BluetoothDevice device = adapter.getRemoteDevice(address);
				
				if (device != null)
				{
					BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString(PRemoteDroidConnection.BLUETOOTH_UUID));
					
					PRemoteDroidConnectionBluetooth connection = new PRemoteDroidConnectionBluetooth(socket);
					
					return connection;
				}
			}
		}
		
		throw new IOException();
	}
	
	public void close() throws IOException
	{
		super.close();
		
		this.socket.close();
	}
}
