package org.pierre.remotedroid.server;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.prefs.Preferences;

import org.pierre.remotedroid.protocol.PRemoteDroidConnection;

public class PRemoteDroidServer implements Runnable
{
	public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
	
	public static final String DEFAULT_PASSWORD = "azerty";
	
	private Preferences preferences;
	private PRemoteDroidServerTrayIcon trayIcon;
	private Robot robot;
	private ServerSocket serverSocket;
	
	public PRemoteDroidServer() throws AWTException, IOException
	{
		this.preferences = Preferences.userNodeForPackage(PRemoteDroidServer.class);
		
		this.robot = new Robot();
		
		int port = this.preferences.getInt("port", PRemoteDroidConnection.DEFAULT_PORT);
		this.serverSocket = new ServerSocket(port);
		
		this.trayIcon = new PRemoteDroidServerTrayIcon(this);
		
		(new Thread(this)).start();
	}
	
	public void exit()
	{
		this.trayIcon.close();
		
		try
		{
			this.serverSocket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		System.exit(0);
	}
	
	public void run()
	{
		try
		{
			while (true)
			{
				new PRemoteDroidServerConnection(new PRemoteDroidConnection(this.serverSocket.accept()), this.robot, this.trayIcon);
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args)
	{
		try
		{
			PRemoteDroidServer server = new PRemoteDroidServer();
		}
		catch (AWTException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
