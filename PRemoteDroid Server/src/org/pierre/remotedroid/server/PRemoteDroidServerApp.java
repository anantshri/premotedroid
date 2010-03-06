package org.pierre.remotedroid.server;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.IOException;
import java.util.prefs.Preferences;

import org.pierre.remotedroid.server.connection.PRemoteDroidServerBluetooth;
import org.pierre.remotedroid.server.connection.PRemoteDroidServerTcp;
import org.pierre.remotedroid.server.gui.PRemoteDroidServerTrayIcon;

public class PRemoteDroidServerApp
{
	public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
	
	private Preferences preferences;
	private PRemoteDroidServerTrayIcon trayIcon;
	private Robot robot;
	
	private PRemoteDroidServerTcp serverTcp;
	private PRemoteDroidServerBluetooth serverBluetooth;
	
	public PRemoteDroidServerApp() throws AWTException, IOException
	{
		this.preferences = Preferences.userNodeForPackage(this.getClass());
		
		this.robot = new Robot();
		
		this.trayIcon = new PRemoteDroidServerTrayIcon(this);
		
		try
		{
			this.serverTcp = new PRemoteDroidServerTcp(this);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			this.serverBluetooth = new PRemoteDroidServerBluetooth(this);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public Preferences getPreferences()
	{
		return preferences;
	}
	
	public PRemoteDroidServerTrayIcon getTrayIcon()
	{
		return trayIcon;
	}
	
	public Robot getRobot()
	{
		return robot;
	}
	
	public PRemoteDroidServerTcp getServerTcp()
	{
		return serverTcp;
	}
	
	public PRemoteDroidServerBluetooth getServerBluetooth()
	{
		return serverBluetooth;
	}
	
	public void exit()
	{
		this.trayIcon.close();
		
		if (this.serverTcp != null)
		{
			this.serverTcp.close();
		}
		
		if (this.serverBluetooth != null)
		{
			this.serverBluetooth.close();
		}
		
		System.exit(0);
	}
	
	public static void main(String[] args)
	{
		try
		{
			PRemoteDroidServerApp application = new PRemoteDroidServerApp();
		}
		catch (AWTException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
}
