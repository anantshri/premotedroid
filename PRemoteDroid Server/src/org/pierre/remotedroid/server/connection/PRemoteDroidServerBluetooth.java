package org.pierre.remotedroid.server.connection;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import org.pierre.remotedroid.protocol.PRemoteDroidConnection;
import org.pierre.remotedroid.protocol.bluetooth.PRemoteDroidConnectionBluetooth;
import org.pierre.remotedroid.server.PRemoteDroidServerApp;

public class PRemoteDroidServerBluetooth extends PRemoteDroidServer implements Runnable
{
	private StreamConnectionNotifier streamConnectionNotifier;
	
	public PRemoteDroidServerBluetooth(PRemoteDroidServerApp application) throws IOException
	{
		super(application);
		
		String uuid = PRemoteDroidConnection.BLUETOOTH_UUID.replaceAll("-", "");
		
		try
		{
			this.streamConnectionNotifier = (StreamConnectionNotifier) Connector.open("btspp://localhost:" + uuid + ";name=PRemoteDroid");
			
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
				PRemoteDroidConnectionBluetooth connection = new PRemoteDroidConnectionBluetooth(streamConnection);
				new PRemoteDroidServerConnection(this.application, connection);
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
			this.streamConnectionNotifier.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
