package org.pierre.remotedroid.server.connection;

import java.io.IOException;

import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;
import javax.obex.HeaderSet;
import javax.obex.Operation;
import javax.obex.ServerRequestHandler;

import org.pierre.remotedroid.protocol.bluetooth.PRemoteDroidConnectionBluetooth;
import org.pierre.remotedroid.server.PRemoteDroidServerApp;

public class PRemoteDroidServerBluetooth extends PRemoteDroidServer implements Runnable
{
	public PRemoteDroidServerBluetooth(PRemoteDroidServerApp application)
	{
		super(application);
		
		(new Thread(this)).start();
	}
	
	public void run()
	{
		try
		{
			UUID uuid = new UUID("1101", true);
			StreamConnectionNotifier scn = (StreamConnectionNotifier) Connector.open("btspp://localhost:" + "300ad0a7059d4d97b9a3eabe5f6af813" + ";name=PRemoteDroid");
			
			while (true)
			{
				StreamConnection streamConnection = scn.acceptAndOpen();
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
