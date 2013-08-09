package com.ControllerDroid.server.connection;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import com.ControllerDroid.protocol.tcp.ControllerDroidConnectionTcp;
import com.ControllerDroid.server.ControllerDroidServerApp;

public class ControllerDroidServerTcp extends ControllerDroidServer implements Runnable
{
	private ServerSocket serverSocket;
	
	public ControllerDroidServerTcp(ControllerDroidServerApp application) throws IOException
	{
		super(application);
		
		int port = this.application.getPreferences().getInt("port", ControllerDroidConnectionTcp.DEFAULT_PORT);
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
				ControllerDroidConnectionTcp connection = new ControllerDroidConnectionTcp(socket);
				new ControllerDroidServerConnection(this.application, connection);
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
