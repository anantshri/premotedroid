package org.pierre.remotedroid.server.connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.pierre.remotedroid.protocol.tcp.PRemoteDroidConnectionTcp;
import org.pierre.remotedroid.server.PRemoteDroidServerApp;

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
			System.out.println("LAN connection broke. This is normal if the server is shutting down.");
			// e.printStackTrace();
		}
	}
	
	public void close()
	{
		try
		{
			if (this.serverSocket != null)
				this.serverSocket.close();
		}
		catch (IOException e)
		{
			System.out.println("Couldn't close the LAN connection. :/ ");
			e.printStackTrace();
		}
	}
	
}
