package org.pierre.remotedroid.protocol.action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;

public abstract class PRemoteDroidAction
{
	public static final byte MOUSE_MOVE = 0;
	public static final byte MOUSE_CLICK = 1;
	public static final byte MOUSE_WHEEL = 2;
	public static final byte KEYBOARD = 3;
	public static final byte AUTHENTIFICATION = 4;
	public static final byte AUTHENTIFICATION_RESPONSE = 5;
	public static final byte SCREEN_CAPTURE_REQUEST = 6;
	public static final byte SCREEN_CAPTURE_RESPONSE = 7;
	
	public InetSocketAddress sender;
	
	public static PRemoteDroidAction parse(DataInputStream dis) throws IOException
	{
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
			default:
				throw new IOException();
		}
	}
	
	public abstract void toDataOutputStream(DataOutputStream dos) throws IOException;
}
