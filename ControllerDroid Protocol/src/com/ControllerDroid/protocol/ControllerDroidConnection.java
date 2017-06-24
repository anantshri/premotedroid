package com.ControllerDroid.protocol;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.ControllerDroid.protocol.action.ControllerDroidAction;
import com.ControllerDroid.protocol.action.ScreenCaptureResponseAction;

public abstract class ControllerDroidConnection
{
	public static final String BLUETOOTH_UUID = "300ad0a7-059d-4d97-b9a3-eabe5f6af813";
	public static final String DEFAULT_PASSWORD = "azerty";
	public static final String DEFAULT_VOLUME_UP_MAPPING = "volumeup";
	public static final String DEFAULT_VOLUME_DOWN_MAPPING = "volumedown";

	private DataInputStream dataInputStream;
	private OutputStream outputStream;
	private ControllerDroidAction capAction = new com.ControllerDroid.protocol.action.ScreenCaptureResponseAction(new byte[3000000]);
	public boolean active = true;
	
	public ControllerDroidConnection(InputStream inputStream, OutputStream outputStream)
	{
		this.dataInputStream = new DataInputStream(inputStream);
		this.outputStream = outputStream;
	}
	
	public ControllerDroidAction receiveAction() throws IOException
	{
		synchronized (this.dataInputStream)
		{
			try
			{
				byte type = this.dataInputStream.readByte();
				
				// SCREEN_CAPTURE_RESPONSE
				if (type == 7)
				{
					return ((ScreenCaptureResponseAction) capAction).parse_(dataInputStream);
				}
				else
				{
					ControllerDroidAction action = ControllerDroidAction.parse(this.dataInputStream, type);
					return action;
				}
			}
			catch (IOException e)
			{
				// Problem with connection (Usually the device disconnected
				active = false;
				throw e;
			}
			
		}
	}
	
	public void sendAction(ControllerDroidAction action) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		action.toDataOutputStream(new DataOutputStream(baos));
		
		synchronized (this.outputStream)
		{
			this.outputStream.write(baos.toByteArray());
			this.outputStream.flush();
		}
	}
	
	public void close() throws IOException
	{
		this.dataInputStream.close();
		this.outputStream.close();
	}
}
