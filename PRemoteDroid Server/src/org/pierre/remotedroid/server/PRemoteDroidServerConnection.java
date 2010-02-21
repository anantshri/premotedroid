package org.pierre.remotedroid.server;

import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;

import org.pierre.remotedroid.protocol.PRemoteDroidConnection;
import org.pierre.remotedroid.protocol.action.AuthentificationAction;
import org.pierre.remotedroid.protocol.action.AuthentificationResponseAction;
import org.pierre.remotedroid.protocol.action.MouseClickAction;
import org.pierre.remotedroid.protocol.action.MouseMoveAction;
import org.pierre.remotedroid.protocol.action.MouseWheelAction;
import org.pierre.remotedroid.protocol.action.PRemoteDroidAction;
import org.pierre.remotedroid.protocol.action.ScreenCaptureRequestAction;
import org.pierre.remotedroid.protocol.action.ScreenCaptureResponseAction;

public class PRemoteDroidServerConnection implements Runnable
{
	private PRemoteDroidConnection connection;
	private Robot robot;
	private PRemoteDroidServerTrayIcon trayIcon;
	
	private Preferences preferences;
	private boolean authentificated;
	
	public PRemoteDroidServerConnection(PRemoteDroidConnection connection, Robot robot, PRemoteDroidServerTrayIcon trayIcon)
	{
		this.connection = connection;
		this.robot = robot;
		this.trayIcon = trayIcon;
		
		this.preferences = Preferences.userNodeForPackage(PRemoteDroidServer.class);
		this.authentificated = false;
		
		(new Thread(this)).start();
	}
	
	public void run()
	{
		try
		{
			try
			{
				while (true)
				{
					PRemoteDroidAction action = this.connection.receiveAction();
					
					this.action(action);
				}
			}
			finally
			{
				this.connection.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void action(PRemoteDroidAction action)
	{
		if (this.authentificated)
		{
			if (action instanceof MouseMoveAction)
			{
				this.moveMouse((MouseMoveAction) action);
			}
			else if (action instanceof MouseClickAction)
			{
				this.mouseClick((MouseClickAction) action);
			}
			else if (action instanceof MouseWheelAction)
			{
				this.mouseWheel((MouseWheelAction) action);
			}
			else if (action instanceof ScreenCaptureRequestAction)
			{
				this.screenCapture((ScreenCaptureRequestAction) action);
			}
		}
		else
		{
			if (action instanceof AuthentificationAction)
			{
				this.authentificate((AuthentificationAction) action);
			}
			
			if (!this.authentificated)
			{
				try
				{
					this.connection.close();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
	
	private void authentificate(AuthentificationAction action)
	{
		if (action.password.equals(this.preferences.get("password", PRemoteDroidServer.DEFAULT_PASSWORD)))
		{
			this.authentificated = true;
			
			this.trayIcon.notifyConnection(action.sender);
		}
		
		this.sendAction(new AuthentificationResponseAction(this.authentificated));
	}
	
	private void moveMouse(MouseMoveAction action)
	{
		Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
		int x = mouseLocation.x + action.moveX;
		int y = mouseLocation.y + action.moveY;
		robot.mouseMove(x, y);
	}
	
	private void mouseClick(MouseClickAction action)
	{
		int button;
		
		switch (action.button)
		{
			case MouseClickAction.BUTTON_LEFT:
				button = InputEvent.BUTTON1_MASK;
				break;
			case MouseClickAction.BUTTON_RIGHT:
				button = InputEvent.BUTTON3_MASK;
				break;
			case MouseClickAction.BUTTON_MIDDLE:
				button = InputEvent.BUTTON2_MASK;
				break;
			default:
				return;
		}
		
		if (action.state == MouseClickAction.STATE_DOWN)
		{
			robot.mousePress(button);
		}
		else if (action.state == MouseClickAction.STATE_UP)
		{
			robot.mouseRelease(button);
		}
		
	}
	
	private void mouseWheel(MouseWheelAction action)
	{
		this.robot.mouseWheel(action.amount);
	}
	
	private void screenCapture(ScreenCaptureRequestAction action)
	{
		try
		{
			Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
			Rectangle r = new Rectangle(mouseLocation.x - (action.width / 2), mouseLocation.y - (action.height / 2), action.width, action.height);
			BufferedImage capture = this.robot.createScreenCapture(r);
			
			String format = null;
			if (action.format == ScreenCaptureRequestAction.FORMAT_PNG)
			{
				format = "png";
			}
			else if (action.format == ScreenCaptureRequestAction.FORMAT_JPG)
			{
				format = "jpg";
			}
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(capture, format, baos);
			byte[] data = baos.toByteArray();
			
			this.sendAction(new ScreenCaptureResponseAction(data));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void sendAction(PRemoteDroidAction action)
	{
		try
		{
			this.connection.sendAction(action);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
