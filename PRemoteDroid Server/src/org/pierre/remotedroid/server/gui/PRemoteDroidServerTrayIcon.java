package org.pierre.remotedroid.server.gui;

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

import org.pierre.remotedroid.protocol.PRemoteDroidConnection;
import org.pierre.remotedroid.protocol.bluetooth.PRemoteDroidConnectionBluetooth;
import org.pierre.remotedroid.protocol.tcp.PRemoteDroidConnectionTcp;
import org.pierre.remotedroid.server.PRemoteDroidServerApp;

public class PRemoteDroidServerTrayIcon
{
	private Preferences preferences;
	private PRemoteDroidServerApp application;
	private TrayIcon trayIcon;
	
	public PRemoteDroidServerTrayIcon(PRemoteDroidServerApp application) throws AWTException, IOException
	{
		this.application = application;
		
		this.preferences = this.application.getPreferences();
		
		this.initTrayIcon();
	}
	
	public void notifyConnection(PRemoteDroidConnection connection)
	{
		String message = "";
		
		if (connection instanceof PRemoteDroidConnectionTcp)
		{
			PRemoteDroidConnectionTcp connectionTcp = (PRemoteDroidConnectionTcp) connection;
			message = connectionTcp.getInetAddress().getHostAddress() + ":" + connectionTcp.getPort();
		}
		else if (connection instanceof PRemoteDroidConnectionBluetooth)
		{
			message = "Bluetooth";
		}
		
		this.trayIcon.displayMessage("PRemoteDroid", "New connection : " + message, MessageType.INFO);
	}
	
	public void notifyProtocolProblem()
	{
		this.trayIcon.displayMessage("PRemoteDroid", "Protocol problem. Please Download the server again", MessageType.INFO);
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
				String password = PRemoteDroidServerTrayIcon.this.preferences.get("password", PRemoteDroidConnection.DEFAULT_PASSWORD);
				password = JOptionPane.showInputDialog("Password", password);
				if (password != null)
				{
					PRemoteDroidServerTrayIcon.this.preferences.put("password", password);
				}
			}
		});
		menu.add(menuItemPassword);
		
		if (PRemoteDroidServerApp.IS_WINDOWS)
		{
			final CheckboxMenuItem menuItemUnicodeWindows = new CheckboxMenuItem("Force disable Unicode Windows alt trick", this.preferences.getBoolean("force_disable_unicode_windows_alt_trick", false));
			menuItemUnicodeWindows.addItemListener(new ItemListener()
			{
				public void itemStateChanged(ItemEvent e)
				{
					PRemoteDroidServerTrayIcon.this.preferences.putBoolean("force_disable_unicode_windows_alt_trick", menuItemUnicodeWindows.getState());
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
				
				if (PRemoteDroidServerTrayIcon.this.application.getServerTcp() != null)
				{
					message.append("Wifi server is listening on :\n");
					message.append(PRemoteDroidServerTrayIcon.this.getTcpListenAddresses());
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
					int port = PRemoteDroidServerTrayIcon.this.preferences.getInt("port", PRemoteDroidConnectionTcp.DEFAULT_PORT);
					String newPortString = JOptionPane.showInputDialog("Port", port);
					int newPort = Integer.parseInt(newPortString);
					PRemoteDroidServerTrayIcon.this.preferences.putInt("port", newPort);
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
				
				if (PRemoteDroidServerTrayIcon.this.application.getServerBluetooth() != null)
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
				PRemoteDroidServerTrayIcon.this.application.exit();
			}
		});
		menu.add(menuItemExit);
		
		this.trayIcon = new TrayIcon(ImageIO.read(this.getClass().getResourceAsStream("icon.png")));
		this.trayIcon.setImageAutoSize(true);
		this.trayIcon.setToolTip("PRemoteDroid server");
		this.trayIcon.setPopupMenu(menu);
		
		SystemTray.getSystemTray().add(this.trayIcon);
		
		StringBuilder message = new StringBuilder("Server started\n");
		message.append(this.getTcpListenAddresses());
		
		this.trayIcon.displayMessage("PRemoteDroid", message.toString(), TrayIcon.MessageType.INFO);
	}
	
	private String getTcpListenAddresses()
	{
		int port = this.preferences.getInt("port", PRemoteDroidConnectionTcp.DEFAULT_PORT);
		
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
