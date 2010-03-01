package org.pierre.remotedroid.server.connection.tcp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.pierre.remotedroid.protocol.PRemoteDroidConnectionTcp;
import org.pierre.remotedroid.server.PRemoteDroidServerApp;
import org.pierre.remotedroid.server.connection.PRemoteDroidServer;
import org.pierre.remotedroid.server.connection.PRemoteDroidServerConnection;

public class PRemoteDroidServerTcp extends PRemoteDroidServer implements Runnable
{
	private ServerSocket serverSocket;
	
	public PRemoteDroidServerTcp(PRemoteDroidServerApp application) throws IOException
	{
		super(application);
		
		int port = this.application.getPreferences().getInt("port", PRemoteDroidConnectionTcp.DEFAULT_PORT);
		this.serverSocket = new ServerSocket(port);
		
		(new Thread(this)).start();
	}
	
	public void run()
	{
		try
		{
			while (true)
			{
				Socket socket = this.serverSocket.accept();
				PRemoteDroidConnectionTcp connection = new PRemoteDroidConnectionTcp(socket);
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
			this.serverSocket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
}
