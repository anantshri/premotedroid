package org.pierre.remotedroid.protocol.action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class MouseClickAction extends PRemoteDroidAction
{
	public static final byte BUTTON_NONE = 0;
	public static final byte BUTTON_LEFT = 1;
	public static final byte BUTTON_RIGHT = 2;
	public static final byte BUTTON_MIDDLE = 3;
	
	public static final boolean STATE_UP = false;
	public static final boolean STATE_DOWN = true;
	
	public byte button;
	public boolean state;
	
	public MouseClickAction(byte button, boolean state)
	{
		this.button = button;
		this.state = state;
	}
	
	public static MouseClickAction parse(DataInputStream dis) throws IOException
	{
		byte button = dis.readByte();
		boolean state = dis.readBoolean();
		
		return new MouseClickAction(button, state);
	}
	
	public void toDataOutputStream(DataOutputStream dos) throws IOException
	{
		dos.writeByte(MOUSE_CLICK);
		dos.writeByte(this.button);
		dos.writeBoolean(this.state);
	}
}
