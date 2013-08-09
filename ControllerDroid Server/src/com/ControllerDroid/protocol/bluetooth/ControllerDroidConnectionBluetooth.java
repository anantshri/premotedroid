package com.ControllerDroid.protocol.bluetooth;

import java.io.IOException;

import javax.microedition.io.StreamConnection;

import com.ControllerDroid.protocol.ControllerDroidConnection;

public class ControllerDroidConnectionBluetooth extends ControllerDroidConnection
{
	private StreamConnection streamConnection;
	
	public ControllerDroidConnectionBluetooth(StreamConnection streamConnection) throws IOException
	{
		super(streamConnection.openInputStream(), streamConnection.openOutputStream());
		
		this.streamConnection = streamConnection;
	}
	
	public void close() throws IOException
	{
		this.streamConnection.close();
		super.close();
	}
}
