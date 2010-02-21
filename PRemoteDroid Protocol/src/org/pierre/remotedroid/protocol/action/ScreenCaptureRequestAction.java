package org.pierre.remotedroid.protocol.action;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class ScreenCaptureRequestAction extends PRemoteDroidAction
{
	public static final byte FORMAT_PNG = 0;
	public static final byte FORMAT_JPG = 1;
	
	public short width;
	public short height;
	public byte format;
	
	public ScreenCaptureRequestAction(short width, short height, byte format)
	{
		this.width = width;
		this.height = height;
		this.format = format;
	}
	
	public static ScreenCaptureRequestAction parse(DataInputStream dis) throws IOException
	{
		short width = dis.readShort();
		short height = dis.readShort();
		byte format = dis.readByte();
		
		return new ScreenCaptureRequestAction(width, height, format);
	}
	
	public void toDataOutputStream(DataOutputStream dos) throws IOException
	{
		dos.writeByte(SCREEN_CAPTURE_REQUEST);
		dos.writeShort(this.width);
		dos.writeShort(this.height);
		dos.writeByte(this.format);
	}
}
