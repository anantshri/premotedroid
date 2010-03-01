package org.pierre.remotedroid.server;

import java.awt.Desktop;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ProtocolException;
import java.util.ArrayList;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;

import org.pierre.remotedroid.protocol.PRemoteDroidTcpConnection;
import org.pierre.remotedroid.protocol.action.AuthentificationAction;
import org.pierre.remotedroid.protocol.action.AuthentificationResponseAction;
import org.pierre.remotedroid.protocol.action.FileExploreRequestAction;
import org.pierre.remotedroid.protocol.action.FileExploreResponseAction;
import org.pierre.remotedroid.protocol.action.KeyboardAction;
import org.pierre.remotedroid.protocol.action.MouseClickAction;
import org.pierre.remotedroid.protocol.action.MouseMoveAction;
import org.pierre.remotedroid.protocol.action.MouseWheelAction;
import org.pierre.remotedroid.protocol.action.PRemoteDroidAction;
import org.pierre.remotedroid.protocol.action.ScreenCaptureRequestAction;
import org.pierre.remotedroid.protocol.action.ScreenCaptureResponseAction;

public class PRemoteDroidServerConnection implements Runnable
{
	private static final int[][] UNICODE_EXCEPTION = {
	        {
	                KeyboardAction.UNICODE_BACKSPACE, KeyEvent.VK_BACK_SPACE
	        }, {
	                10, KeyEvent.VK_ENTER
	        }
	};
	
	private PRemoteDroidTcpConnection connection;
	
	private Preferences preferences;
	private Robot robot;
	private PRemoteDroidServerTrayIcon trayIcon;
	
	private boolean authentificated;
	
	private boolean useUnicodeWindowsAltTrick;
	
	public PRemoteDroidServerConnection(PRemoteDroidTcpConnection connection, Robot robot, PRemoteDroidServerTrayIcon trayIcon)
	{
		this.connection = connection;
		this.robot = robot;
		this.trayIcon = trayIcon;
		
		this.preferences = Preferences.userNodeForPackage(PRemoteDroidServer.class);
		this.authentificated = false;
		
		this.useUnicodeWindowsAltTrick = PRemoteDroidServer.IS_WINDOWS && !this.preferences.getBoolean("force_disable_unicode_windows_alt_trick", false);
		
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
		catch (ProtocolException e)
		{
			e.printStackTrace();
			
			this.trayIcon.notifyProtocolProblem();
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
			else if (action instanceof FileExploreRequestAction)
			{
				this.fileExplore((FileExploreRequestAction) action);
			}
			else if (action instanceof KeyboardAction)
			{
				this.keyboard((KeyboardAction) action);
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
			
			this.trayIcon.notifyConnection(this.connection.getInetAddress(), this.connection.getPort());
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
	
	private void fileExplore(FileExploreRequestAction action)
	{
		if (action.directory.isEmpty() && action.file.isEmpty())
		{
			this.fileExploreRoots();
		}
		else
		{
			if (action.directory.isEmpty())
			{
				this.fileExplore(new File(action.file));
			}
			else
			{
				File directory = new File(action.directory);
				
				if (directory.getParent() == null && action.file.equals(".."))
				{
					this.fileExploreRoots();
				}
				else
				{
					try
					{
						this.fileExplore(new File(directory, action.file).getCanonicalFile());
					}
					catch (IOException e)
					{
						e.printStackTrace();
						
						this.fileExploreRoots();
					}
				}
			}
		}
	}
	
	private void fileExplore(File file)
	{
		if (file.exists() && file.canRead())
		{
			if (file.isDirectory())
			{
				this.sendFileExploreResponse(file.getAbsolutePath(), file.listFiles(), true);
			}
			else
			{
				if (Desktop.isDesktopSupported())
				{
					Desktop desktop = Desktop.getDesktop();
					
					if (desktop.isSupported(Desktop.Action.OPEN))
					{
						try
						{
							desktop.open(file);
						}
						catch (IOException e)
						{
							e.printStackTrace();
							
							if (PRemoteDroidServer.IS_WINDOWS)
							{
								System.out.println("windows cmd fix");
								
								try
								{
									Process process = Runtime.getRuntime().exec("cmd /C " + file.getAbsolutePath());
									BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
									
									String line;
									while ((line = br.readLine()) != null)
									{
										System.out.println(line);
									}
								}
								catch (IOException e1)
								{
									e1.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
		else
		{
			this.fileExploreRoots();
		}
	}
	
	private void fileExploreRoots()
	{
		String directory = "";
		
		File[] files = File.listRoots();
		
		this.sendFileExploreResponse(directory, files, false);
	}
	
	private void sendFileExploreResponse(String directory, File[] f, boolean parent)
	{
		if (f != null)
		{
			ArrayList<String> list = new ArrayList<String>();
			
			if (parent)
			{
				list.add("..");
			}
			
			for (int i = 0; i < f.length; i++)
			{
				String name = f[i].getName();
				
				if (!name.isEmpty())
				{
					if (f[i].isDirectory())
					{
						name += File.separator;
					}
				}
				else
				{
					name = f[i].getAbsolutePath();
				}
				
				list.add(name);
			}
			
			String[] files = new String[list.size()];
			
			files = list.toArray(files);
			
			this.sendAction(new FileExploreResponseAction(directory, files));
		}
	}
	
	private void keyboard(KeyboardAction action)
	{
		if (this.useUnicodeWindowsAltTrick)
		{
			this.keyboardUnicodeWindowsAltTrick(action);
		}
		else
		{
			this.keyboardClassic(action);
		}
	}
	
	private void keyboardUnicodeWindowsAltTrick(KeyboardAction action)
	{
		boolean exception = false;
		
		for (int i = 0; i < UNICODE_EXCEPTION.length; i++)
		{
			if (action.unicode == UNICODE_EXCEPTION[i][0])
			{
				exception = true;
				
				this.robot.keyPress(UNICODE_EXCEPTION[i][1]);
				this.robot.keyRelease(UNICODE_EXCEPTION[i][1]);
				
				break;
			}
		}
		
		if (!exception)
		{
			this.robot.keyPress(KeyEvent.VK_ALT);
			
			String unicodeString = Integer.toString(action.unicode);
			
			for (int i = 0; i < unicodeString.length(); i++)
			{
				int digit = Integer.parseInt(unicodeString.substring(i, i + 1));
				int keycode = digit + KeyEvent.VK_NUMPAD0;
				this.robot.keyPress(keycode);
				this.robot.keyRelease(keycode);
			}
			
			this.robot.keyRelease(KeyEvent.VK_ALT);
		}
	}
	
	private void keyboardClassic(KeyboardAction action)
	{
		int keycode = UnicodeToSwingKeyCodeConverter.convert(action.unicode);
		
		if (keycode != UnicodeToSwingKeyCodeConverter.NO_SWING_KEYCODE)
		{
			boolean useShift = UnicodeToSwingKeyCodeConverter.useShift(action.unicode);
			
			if (useShift)
			{
				this.robot.keyPress(KeyEvent.VK_SHIFT);
			}
			
			this.robot.keyPress(keycode);
			this.robot.keyRelease(keycode);
			
			if (useShift)
			{
				this.robot.keyRelease(KeyEvent.VK_SHIFT);
			}
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
