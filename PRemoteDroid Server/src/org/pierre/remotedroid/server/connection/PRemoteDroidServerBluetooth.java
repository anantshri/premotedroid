package org.pierre.remotedroid.server.connection;

import java.io.IOException;

import javax.microedition.io.Connector;
import javax.obex.ServerRequestHandler;
import javax.obex.SessionNotifier;

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
			SessionNotifier serverConnection = (SessionNotifier) Connector.open("btgoep://localhost:" + "300ad0a7059d4d97b9a3eabe5f6af813" + ";name=PRemoteDroid");
			ServerRequestHandler handler = new ServerRequestHandler()
			{
			};
			while (true)
			{
				serverConnection.acceptAndOpen(handler);
				System.out.println("lol");
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
}
