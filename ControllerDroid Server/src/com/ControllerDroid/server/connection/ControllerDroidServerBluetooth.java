package com.ControllerDroid.server.connection;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import com.ControllerDroid.protocol.ControllerDroidConnection;
import com.ControllerDroid.protocol.bluetooth.ControllerDroidConnectionBluetooth;
import com.ControllerDroid.server.ControllerDroidServerApp;

public class ControllerDroidServerBluetooth extends ControllerDroidServer implements Runnable
{
	private StreamConnectionNotifier streamConnectionNotifier;
	
	public ControllerDroidServerBluetooth(ControllerDroidServerApp application) throws IOException
	{
		super(application);
		
		String uuid = ControllerDroidConnection.BLUETOOTH_UUID.replaceAll("-", "");
		
		try
		{
			this.streamConnectionNotifier = (StreamConnectionNotifier) Connector.open("btspp://localhost:" + uuid + ";name=ControllerDroid");
			
			if (this.streamConnectionNotifier != null)
			{
				(new Thread(this)).start();
			}
			else
			{
				throw new IOException();
			}
		}
		catch (Exception e)
		{
			// Could not start Bluetooth services.
			// TODO: Do something here to disable the bluetooth option, or
			// provide a nice error message.
			
		}
	}
	
	public void run()
	{
		try
		{
			while (true)
			{
				StreamConnection streamConnection = streamConnectionNotifier.acceptAndOpen();
				ControllerDroidConnectionBluetooth connection = new ControllerDroidConnectionBluetooth(streamConnection);
				new ControllerDroidServerConnection(this.application, connection);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void close()
	{
		try
		{
			if (this.streamConnectionNotifier != null)
				this.streamConnectionNotifier.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
