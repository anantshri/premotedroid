package org.pierre.remotedroid.server.connection;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ServerRequestHandler;

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
		this.streamConnectionNotifier = (StreamConnectionNotifier) Connector.open("btspp://localhost:" + uuid + ";name=PRemoteDroid");
		
		(new Thread(this)).start();
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
	}
	
	private class BluetoothHandler extends ServerRequestHandler
	{
		@Override
		public void onAuthenticationFailure(byte[] userName)
		{
			System.out.println("1");
			super.onAuthenticationFailure(userName);
		}
		
		@Override
		public int onConnect(HeaderSet request, HeaderSet reply)
		{
			System.out.println("2");
			return super.onConnect(request, reply);
		}
		
		@Override
		public int onDelete(HeaderSet request, HeaderSet reply)
		{
			System.out.println("3");
			return super.onDelete(request, reply);
		}
		
		@Override
		public void onDisconnect(HeaderSet request, HeaderSet reply)
		{
			System.out.println("4");
			super.onDisconnect(request, reply);
		}
		
		@Override
		public int onGet(Operation op)
		{
			System.out.println("5");
			return super.onGet(op);
		}
		
		@Override
		public int onPut(Operation op)
		{
			System.out.println("6");
			return super.onPut(op);
		}
		
		@Override
		public int onSetPath(HeaderSet request, HeaderSet reply, boolean backup, boolean create)
		{
			System.out.println("7");
			return super.onSetPath(request, reply, backup, create);
		}
	}
}
