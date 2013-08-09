package com.ControllerDroid.server.gui;

import java.awt.AWTException;
import java.awt.CheckboxMenuItem;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import com.ControllerDroid.protocol.ControllerDroidConnection;
import com.ControllerDroid.protocol.bluetooth.ControllerDroidConnectionBluetooth;
import com.ControllerDroid.protocol.tcp.ControllerDroidConnectionTcp;
import com.ControllerDroid.server.ControllerDroidServerApp;

public class ControllerDroidServerTrayIcon
{
	private Preferences preferences;
	private ControllerDroidServerApp application;
	private TrayIcon trayIcon;
	
	public ControllerDroidServerTrayIcon(ControllerDroidServerApp application) throws AWTException, IOException
	{
		this.application = application;
		
		this.preferences = this.application.getPreferences();
		
		this.initTrayIcon();
	}
	
	public void notifyConnection(ControllerDroidConnection connection)
	{
		String message = "";
		
		if (connection instanceof ControllerDroidConnectionTcp)
		{
			ControllerDroidConnectionTcp connectionTcp = (ControllerDroidConnectionTcp) connection;
			message = connectionTcp.getInetAddress().getHostAddress() + ":" + connectionTcp.getPort();
		}
		else if (connection instanceof ControllerDroidConnectionBluetooth)
		{
			
			message = "Bluetooth";
		}
		
		this.trayIcon.displayMessage("ControllerDroid", (connection.active ? "New connection : " : "Device Disconnected: ") + message, MessageType.INFO);
	}
	
	public void notifyProtocolProblem()
	{
		this.trayIcon.displayMessage("ControllerDroid", "Protocol problem. Please Download the server again", MessageType.INFO);
	}
	
	public void close()
	{
		SystemTray.getSystemTray().remove(this.trayIcon);
	}
	
	private void initTrayIcon() throws AWTException, IOException
	{
		PopupMenu menu = new PopupMenu();
		
		MenuItem menuItemPassword = new MenuItem("Password");
		menuItemPassword.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				String password = ControllerDroidServerTrayIcon.this.preferences.get("password", ControllerDroidConnection.DEFAULT_PASSWORD);
				password = JOptionPane.showInputDialog("Password", password);
				if (password != null)
				{
					ControllerDroidServerTrayIcon.this.preferences.put("password", password);
				}
			}
		});
		menu.add(menuItemPassword);
		
		if (ControllerDroidServerApp.IS_WINDOWS)
		{
			final CheckboxMenuItem menuItemUnicodeWindows = new CheckboxMenuItem("Force disable Unicode Windows alt trick", this.preferences.getBoolean("force_disable_unicode_windows_alt_trick", false));
			menuItemUnicodeWindows.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					ControllerDroidServerTrayIcon.this.preferences.putBoolean("force_disable_unicode_windows_alt_trick", menuItemUnicodeWindows.getState());
					JOptionPane.showMessageDialog(null, "Restart the connection to apply this preference.");
				}
			});
			menu.add(menuItemUnicodeWindows);
		}
		
		menu.addSeparator();
		
		MenuItem menuItemWifiServer = new MenuItem("Wifi Server");
		menuItemWifiServer.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				StringBuilder message = new StringBuilder();
				
				if (ControllerDroidServerTrayIcon.this.getTcpListenAddresses() != null)
				{
					message.append("Wifi server is listening on :\n");
					message.append(ControllerDroidServerTrayIcon.this.getTcpListenAddresses());
				}
				else
				{
					message.append("Wifi server is not running");
				}
				
				JOptionPane.showMessageDialog(null, message.toString());
			}
		});
		menu.add(menuItemWifiServer);
		
		MenuItem menuItemPort = new MenuItem("Port");
		menuItemPort.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				try
				{
					int port = ControllerDroidServerTrayIcon.this.preferences.getInt("port", ControllerDroidConnectionTcp.DEFAULT_PORT);
					String newPortString = JOptionPane.showInputDialog("Port", port);
					int newPort = Integer.parseInt(newPortString);
					ControllerDroidServerTrayIcon.this.preferences.putInt("port", newPort);
					JOptionPane.showMessageDialog(null, "Restart the server to apply the new port.");
				}
				catch (NumberFormatException nfe)
				{
					nfe.printStackTrace();
				}
			}
		});
		menu.add(menuItemPort);
		
		menu.addSeparator();
		
		MenuItem menuItemBluetoothServer = new MenuItem("Bluetooth Server");
		menuItemBluetoothServer.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				StringBuilder message = new StringBuilder();
				
				if (ControllerDroidServerTrayIcon.this.application.getServerBluetooth() != null)
				{
					message.append("Bluetooth server is running");
				}
				else
				{
					message.append("Bluetooth server is not running");
				}
				
				JOptionPane.showMessageDialog(null, message.toString());
			}
		});
		menu.add(menuItemBluetoothServer);
		
		menu.addSeparator();
		
		MenuItem menuItemExit = new MenuItem("Exit");
		menuItemExit.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent arg0)
			{
				ControllerDroidServerTrayIcon.this.application.exit();
			}
		});
		menu.add(menuItemExit);
		
		this.trayIcon = new TrayIcon(ImageIO.read(this.getClass().getResourceAsStream("icon.png")));
		this.trayIcon.setImageAutoSize(true);
		this.trayIcon.setToolTip("ControllerDroid server");
		this.trayIcon.setPopupMenu(menu);
		
		SystemTray.getSystemTray().add(this.trayIcon);
		
		StringBuilder message = new StringBuilder("Server started\n");
		message.append(this.getTcpListenAddresses());
		
		this.trayIcon.displayMessage("ControllerDroid", message.toString(), TrayIcon.MessageType.INFO);
	}
	
	private String getTcpListenAddresses()
	{
		int port = this.preferences.getInt("port", ControllerDroidConnectionTcp.DEFAULT_PORT);
		
		StringBuilder message = new StringBuilder();
		
		try
		{
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements())
			{
				NetworkInterface currentInterface = interfaces.nextElement();
				
				Enumeration<InetAddress> addresses = currentInterface.getInetAddresses();
				
				while (addresses.hasMoreElements())
				{
					InetAddress currentAddress = addresses.nextElement();
					
					if (!currentAddress.isLoopbackAddress() && !(currentAddress instanceof Inet6Address))
					{
						message.append(currentAddress.getHostAddress() + ":" + port + "\n");
					}
				}
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		return message.toString();
	}
}
