package org.pierre.remotedroid.protocol.action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class KeyboardAction extends PRemoteDroidAction
{
	public static final boolean STATE_UP = false;
	public static final boolean STATE_DOWN = true;
	
	public boolean state;
	public int swingKeyCode;
	
	public KeyboardAction(boolean state, int swingKeyCode)
	{
		this.state = state;
		this.swingKeyCode = swingKeyCode;
	}
	
	public static KeyboardAction parse(DataInputStream dis) throws IOException
	{
		boolean state = dis.readBoolean();
		int swingKeyCode = dis.readInt();
		
		return new KeyboardAction(state, swingKeyCode);
	}
	
	public void toDataOutputStream(DataOutputStream dos) throws IOException
	{
		dos.writeByte(KEYBOARD);
		dos.writeBoolean(this.state);
		dos.writeInt(this.swingKeyCode);
	}
}
