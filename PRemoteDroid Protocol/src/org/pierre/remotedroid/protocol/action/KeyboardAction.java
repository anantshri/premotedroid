package org.pierre.remotedroid.protocol.action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class KeyboardAction extends PRemoteDroidAction
{
	public boolean state;
	public int key;
	
	public KeyboardAction(boolean state, int key)
	{
		this.state = state;
		this.key = key;
	}
	
	public static KeyboardAction parse(DataInputStream dis) throws IOException
	{
		boolean state = dis.readBoolean();
		int key = dis.readInt();
		
		return new KeyboardAction(state, key);
	}
	
	public void toDataOutputStream(DataOutputStream dos) throws IOException
	{
		dos.writeByte(KEYBOARD);
		dos.writeBoolean(this.state);
		dos.writeInt(this.key);
	}
}
