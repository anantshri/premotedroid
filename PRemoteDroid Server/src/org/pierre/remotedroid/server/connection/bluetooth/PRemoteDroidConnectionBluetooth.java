package org.pierre.remotedroid.server.connection.bluetooth;

import java.io.InputStream;
import java.io.OutputStream;

import org.pierre.remotedroid.protocol.PRemoteDroidConnection;

public class PRemoteDroidConnectionBluetooth extends PRemoteDroidConnection
{
	public PRemoteDroidConnectionBluetooth(InputStream inputStream, OutputStream outputStream)
	{
		super(inputStream, outputStream);
	}
}
