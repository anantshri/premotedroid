package org.pierre.remotedroid.protocol.action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ScreenCaptureResponseAction extends PRemoteDroidAction
{
	public byte[] data;// = new byte[100000];
	public int dataSize = 0;
	
	public ScreenCaptureResponseAction(byte[] data)
	{
		this.data = data;
		this.dataSize = data.length;
	}
	
	public ScreenCaptureResponseAction(byte[] data, int dataSize)
	{
		this.data = data;
		this.dataSize = dataSize;
	}
	
	public static ScreenCaptureResponseAction parse(DataInputStream dis) throws IOException
	{
		int dataSize = dis.readInt();
		byte[] data = new byte[dataSize];
		dis.readFully(data);
		android.util.Log.d("Note", "Used Dep method!");
		return new ScreenCaptureResponseAction(data, dataSize);
	}
	
	public ScreenCaptureResponseAction parse_(DataInputStream dis) throws IOException
	{
		this.dataSize = dis.readInt();
		dis.readFully(this.data, 0, this.dataSize);
		// dis.skipBytes(this.dataSize);
		return this;
	}
	
	public void toDataOutputStream(DataOutputStream dos) throws IOException
	{
		dos.writeByte(SCREEN_CAPTURE_RESPONSE);
		dos.writeInt(this.data.length);
		dos.write(this.data);
	}
}