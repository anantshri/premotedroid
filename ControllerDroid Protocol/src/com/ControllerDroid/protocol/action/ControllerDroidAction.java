package com.ControllerDroid.protocol.action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ProtocolException;

public abstract class ControllerDroidAction
{
	public static final byte MOUSE_MOVE = 0;
	public static final byte MOUSE_CLICK = 1;
	public static final byte MOUSE_WHEEL = 2;
	public static final byte KEYBOARD = 3;
	public static final byte AUTHENTIFICATION = 4;
	public static final byte AUTHENTIFICATION_RESPONSE = 5;
	public static final byte SCREEN_CAPTURE_REQUEST = 6;
	public static final byte SCREEN_CAPTURE_RESPONSE = 7;
	public static final byte FILE_EXPLORE_REQUEST = 8;
	public static final byte FILE_EXPLORE_RESPONSE = 9;
	
	public static ControllerDroidAction parse(DataInputStream dis) throws IOException
	{
		// This method shouldn't be called in the Client anymore. It's for the
		// Server portion only
		byte type = dis.readByte();
		
		switch (type)
		{
			case MOUSE_MOVE:
				return MouseMoveAction.parse(dis);
			case MOUSE_CLICK:
				return MouseClickAction.parse(dis);
			case MOUSE_WHEEL:
				return MouseWheelAction.parse(dis);
			case KEYBOARD:
				return KeyboardAction.parse(dis);
			case AUTHENTIFICATION:
				return AuthentificationAction.parse(dis);
			case AUTHENTIFICATION_RESPONSE:
				return AuthentificationResponseAction.parse(dis);
			case SCREEN_CAPTURE_REQUEST:
				return ScreenCaptureRequestAction.parse(dis);
			case SCREEN_CAPTURE_RESPONSE:
				return ScreenCaptureResponseAction.parse(dis);
			case FILE_EXPLORE_REQUEST:
				return FileExploreRequestAction.parse(dis);
			case FILE_EXPLORE_RESPONSE:
				return FileExploreResponseAction.parse(dis);
			default:
				throw new ProtocolException();
		}
	}
	
	public static ControllerDroidAction parse(DataInputStream dis, byte type) throws IOException
	{
		// byte type = dis.readByte();
		
		switch (type)
		{
			case MOUSE_MOVE:
				return MouseMoveAction.parse(dis);
			case MOUSE_CLICK:
				return MouseClickAction.parse(dis);
			case MOUSE_WHEEL:
				return MouseWheelAction.parse(dis);
			case KEYBOARD:
				return KeyboardAction.parse(dis);
			case AUTHENTIFICATION:
				return AuthentificationAction.parse(dis);
			case AUTHENTIFICATION_RESPONSE:
				return AuthentificationResponseAction.parse(dis);
			case SCREEN_CAPTURE_REQUEST:
				return ScreenCaptureRequestAction.parse(dis);
			case SCREEN_CAPTURE_RESPONSE:
				// This should never happen anymore
				// return ScreenCaptureResponseAction.parse(dis);
				return new ScreenCaptureResponseAction(new byte[0]);
			case FILE_EXPLORE_REQUEST:
				return FileExploreRequestAction.parse(dis);
			case FILE_EXPLORE_RESPONSE:
				return FileExploreResponseAction.parse(dis);
			default:
				// Ignore protocol errors for now.
				return new ScreenCaptureResponseAction(new byte[0]);
				// throw new ProtocolException();
		}
	}
	
	public abstract void toDataOutputStream(DataOutputStream dos) throws IOException;
}
