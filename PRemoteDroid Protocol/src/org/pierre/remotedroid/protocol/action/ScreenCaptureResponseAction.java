package org.pierre.remotedroid.protocol.action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ScreenCaptureResponseAction extends PRemoteDroidAction
{
	public byte[] data;
	
	public ScreenCaptureResponseAction(byte[] data)
	{
		this.data = data;
	}
	
	public static ScreenCaptureResponseAction parse(DataInputStream dis) throws IOException
	{
		int dataSize = dis.readInt();
		byte[] data = new byte[dataSize];
		dis.readFully(data);
		
		return new ScreenCaptureResponseAction(data);
	}
	
	public void toDataOutputStream(DataOutputStream dos) throws IOException
	{
		dos.writeByte(SCREEN_CAPTURE_RESPONSE);
		dos.writeInt(this.data.length);
		dos.write(this.data);
	}
}