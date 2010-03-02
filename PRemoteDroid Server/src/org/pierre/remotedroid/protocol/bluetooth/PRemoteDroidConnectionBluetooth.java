package org.pierre.remotedroid.protocol.bluetooth;

import java.io.IOException;

import javax.microedition.io.StreamConnection;

import org.pierre.remotedroid.protocol.PRemoteDroidConnection;

public class PRemoteDroidConnectionBluetooth extends PRemoteDroidConnection
{
	private StreamConnection streamConnection;
	
	public PRemoteDroidConnectionBluetooth(StreamConnection streamConnection) throws IOException
	{
		super(streamConnection.openInputStream(), streamConnection.openOutputStream());
		
		this.streamConnection = streamConnection;
	}
	
	public void close() throws IOException
	{
		super.close();
		
		this.streamConnection.close();
	}
}
