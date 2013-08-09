package com.ControllerDroid.server.connection;

import java.awt.Desktop;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
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

import javax.imageio.ImageIO;

import com.ControllerDroid.protocol.ControllerDroidConnection;
import com.ControllerDroid.protocol.action.AuthentificationAction;
import com.ControllerDroid.protocol.action.AuthentificationResponseAction;
import com.ControllerDroid.protocol.action.FileExploreRequestAction;
import com.ControllerDroid.protocol.action.FileExploreResponseAction;
import com.ControllerDroid.protocol.action.KeyboardAction;
import com.ControllerDroid.protocol.action.MouseClickAction;
import com.ControllerDroid.protocol.action.MouseMoveAction;
import com.ControllerDroid.protocol.action.MouseWheelAction;
import com.ControllerDroid.protocol.action.ControllerDroidAction;
import com.ControllerDroid.protocol.action.ScreenCaptureRequestAction;
import com.ControllerDroid.protocol.action.ScreenCaptureResponseAction;
import com.ControllerDroid.server.ControllerDroidServerApp;
import com.ControllerDroid.server.tools.UnicodeToSwingKeyCodeConverter;

public class ControllerDroidServerConnection implements Runnable
{
	private static final int[][] UNICODE_EXCEPTION = {
	        {
	                KeyboardAction.UNICODE_BACKSPACE, KeyEvent.VK_BACK_SPACE
	        }, {
	                10, KeyEvent.VK_ENTER
	        }, {
	                KeyboardAction.UNICODE_PAGEDN, KeyEvent.VK_PAGE_DOWN
	        }, {
	                KeyboardAction.UNICODE_PAGEUP, KeyEvent.VK_PAGE_UP
	        }, {
	                KeyboardAction.UNICODE_TAB, KeyEvent.VK_TAB
	        }, {
	                KeyboardAction.UNICODE_ARROW_DOWN, KeyEvent.VK_DOWN
	        }, {
	                KeyboardAction.UNICODE_ARROW_UP, KeyEvent.VK_UP
	        }, {
	                KeyboardAction.UNICODE_ARROW_LEFT, KeyEvent.VK_LEFT
	        }, {
	                KeyboardAction.UNICODE_ARROW_RIGHT, KeyEvent.VK_RIGHT
	        }
	};
	
	private ControllerDroidServerApp application;
	
	private ControllerDroidConnection connection;
	
	private boolean authentificated;
	
	private boolean useUnicodeWindowsAltTrick;
	
	public ControllerDroidServerConnection(ControllerDroidServerApp application, ControllerDroidConnection connection)
	{
		this.application = application;
		this.connection = connection;
		
		this.authentificated = false;
		
		this.useUnicodeWindowsAltTrick = ControllerDroidServerApp.IS_WINDOWS && !this.application.getPreferences().getBoolean("force_disable_unicode_windows_alt_trick", false);
		
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
					ControllerDroidAction action = this.connection.receiveAction();
					
					this.action(action);
				}
			}
			catch (IOException e)
			{
				// Happens when the client disconnects
				this.application.getTrayIcon().notifyConnection(this.connection);
			}
			finally
			{
				this.connection.close();
			}
		}
		catch (ProtocolException e)
		{
			e.printStackTrace();
			
			this.application.getTrayIcon().notifyProtocolProblem();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	private void action(ControllerDroidAction action)
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
		if (action.password.equals(this.application.getPreferences().get("password", ControllerDroidConnection.DEFAULT_PASSWORD)))
		{
			this.authentificated = true;
			
			this.application.getTrayIcon().notifyConnection(this.connection);
		}
		
		this.sendAction(new AuthentificationResponseAction(this.authentificated));
	}
	
	private void moveMouse(MouseMoveAction action)
	{
		PointerInfo pointerInfo = MouseInfo.getPointerInfo();
		
		if (pointerInfo != null)
		{
			Point mouseLocation = pointerInfo.getLocation();
			
			if (mouseLocation != null)
			{
				int x = mouseLocation.x + action.moveX;
				int y = mouseLocation.y + action.moveY;
				this.application.getRobot().mouseMove(x, y);
			}
		}
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
			this.application.getRobot().mousePress(button);
		}
		else if (action.state == MouseClickAction.STATE_UP)
		{
			this.application.getRobot().mouseRelease(button);
		}
		
	}
	
	private void mouseWheel(MouseWheelAction action)
	{
		this.application.getRobot().mouseWheel(action.amount);
	}
	
	private void screenCapture(ScreenCaptureRequestAction action)
	{
		try
		{
			Point mouseLocation = MouseInfo.getPointerInfo().getLocation();
			Rectangle r = new Rectangle(mouseLocation.x - (action.width / 2), mouseLocation.y - (action.height / 2), action.width, action.height);
			BufferedImage capture = new BufferedImage(1, 1, 12);
			if (r.width + r.height > 0)
				capture = this.application.getRobot().createScreenCapture(r);
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
							
							if (ControllerDroidServerApp.IS_WINDOWS)
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
				
				this.application.getRobot().keyPress(UNICODE_EXCEPTION[i][1]);
				this.application.getRobot().keyRelease(UNICODE_EXCEPTION[i][1]);
				
				break;
			}
		}
		
		if (!exception)
		{
			pressUnicode(this.application.getRobot(), action.unicode);
		}
	}
	
	private void keyboardClassic(KeyboardAction action)
	{
		int keycode = UnicodeToSwingKeyCodeConverter.convert(action.unicode);
		if (keycode != UnicodeToSwingKeyCodeConverter.NO_SWING_KEYCODE)
		{
			// System.out.println("Now pressing key of code: " + keycode);
			
			boolean useShift = UnicodeToSwingKeyCodeConverter.useShift(action.unicode);
			
			if (useShift)
			{
				this.application.getRobot().keyPress(KeyEvent.VK_SHIFT);
			}
			
			this.application.getRobot().keyPress(keycode);
			this.application.getRobot().keyRelease(keycode);
			
			if (useShift)
			{
				this.application.getRobot().keyRelease(KeyEvent.VK_SHIFT);
			}
		}
		else
		{
			// Not normal character, use Unicode
			pressUnicode(this.application.getRobot(), action.unicode);
		}
	}
	
	public static void pressUnicode(java.awt.Robot r, int key_code)
	{
		r.keyPress(KeyEvent.VK_ALT);
		boolean useHexKeypad = false;
		if (!useHexKeypad)
		{
			String s = String.format("%04d", key_code);
			// System.out.println("Now pressing key of dec unicode: " + s);
			for (int i = 0; i < s.length(); i++)
			{
				char c = s.charAt(i);
				int keycode = c - '0' + KeyEvent.VK_NUMPAD0;
				
				r.keyPress(keycode);
				r.keyRelease(keycode);
			}
		}
		else
		{
			String s = Integer.toHexString(key_code);
			// System.out.println("Now pressing key of hex unicode: " + s);
			r.keyPress(0x6b);
			r.keyRelease(0x6b);
			for (int i = 0; i < s.length(); i++)
			{
				int keycode = UnicodeToSwingKeyCodeConverter.convert(s.charAt(i));
				
				r.keyPress(keycode);
				r.keyRelease(keycode);
			}
		}
		r.keyRelease(KeyEvent.VK_ALT);
	}
	
	private void sendAction(ControllerDroidAction action)
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
