package com.ControllerDroid.server;

import java.awt.AWTException;
import java.awt.Robot;
import java.io.IOException;
import java.util.prefs.Preferences;

import com.ControllerDroid.server.connection.ControllerDroidServerBluetooth;
import com.ControllerDroid.server.connection.ControllerDroidServerTcp;
import com.ControllerDroid.server.gui.ControllerDroidServerTrayIcon;

public class ControllerDroidServerApp
{
	public static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
	public static final boolean IS_LINUX   = System.getProperty("os.name").toLowerCase().contains("linux");
	
	private Preferences preferences;
	private ControllerDroidServerTrayIcon trayIcon;
	private Robot robot;
	
	private ControllerDroidServerTcp serverTcp;
	private ControllerDroidServerBluetooth serverBluetooth;
	
	public ControllerDroidServerApp() throws AWTException, IOException
	{
		this.preferences = Preferences.userNodeForPackage(this.getClass());
		
		this.robot = new Robot();
		
		this.trayIcon = new ControllerDroidServerTrayIcon(this);
		
		try
		{
			this.serverTcp = new ControllerDroidServerTcp(this);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		try
		{
			this.serverBluetooth = new ControllerDroidServerBluetooth(this);
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
	
	public ControllerDroidServerTrayIcon getTrayIcon()
	{
		return trayIcon;
	}
	
	public Robot getRobot()
	{
		return robot;
	}
	
	public ControllerDroidServerTcp getServerTcp()
	{
		return serverTcp;
	}
	
	public ControllerDroidServerBluetooth getServerBluetooth()
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
			ControllerDroidServerApp application = new ControllerDroidServerApp();
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
