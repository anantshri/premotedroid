package org.pierre.remotedroid.protocol.bluetooth;

import java.io.IOException;
import java.util.UUID;

import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.client.app.PRemoteDroid;
import org.pierre.remotedroid.protocol.PRemoteDroidConnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Looper;

public class PRemoteDroidConnectionBluetooth extends PRemoteDroidConnection
{
	private BluetoothSocket socket;
	
	public PRemoteDroidConnectionBluetooth(BluetoothSocket socket) throws IOException
	{
		super(socket.getInputStream(), socket.getOutputStream());
		
		this.socket = socket;
	}
	
	public static PRemoteDroidConnectionBluetooth create(PRemoteDroid application, String address) throws IOException
	{
		Looper.prepare();
		
		BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
		
		if (adapter != null)
		{
			if (adapter.isEnabled())
			{
				BluetoothDevice device = adapter.getRemoteDevice(address);
				
				if (device != null)
				{
					try
					{
						BluetoothSocket socket = device.createRfcommSocketToServiceRecord(UUID.fromString(PRemoteDroidConnection.BLUETOOTH_UUID));
						socket.connect();
						
						PRemoteDroidConnectionBluetooth connection = new PRemoteDroidConnectionBluetooth(socket);
						
						return connection;
					}
					catch (IOException e)
					{
						application.showInternalToast(R.string.text_bluetooth_can_not_connect);
						
						throw e;
					}
				}
				else
				{
					application.showInternalToast(R.string.text_bluetooth_remote_device_not_found);
					
					throw new IOException();
				}
			}
			else
			{
				application.showInternalToast(R.string.text_bluetooth_not_enabled);
				
				throw new IOException();
			}
		}
		else
		{
			application.showInternalToast(R.string.text_bluetooth_not_available);
			
			throw new IOException();
		}
	}
	
	public void close() throws IOException
	{
		this.socket.close();
	}
}
