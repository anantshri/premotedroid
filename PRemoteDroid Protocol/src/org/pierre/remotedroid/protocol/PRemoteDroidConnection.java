package org.pierre.remotedroid.protocol;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import org.pierre.remotedroid.protocol.action.PRemoteDroidAction;

public class PRemoteDroidConnection
{
	public final static int DEFAULT_PORT = 64788;
	
	private Socket socket;
	
	private DataInputStream dataInputStream;
	
	public PRemoteDroidConnection(Socket socket) throws IOException
	{
		this.socket = socket;
		this.socket.setPerformancePreferences(0, 2, 1);
		this.socket.setTcpNoDelay(true);
		this.socket.setReceiveBufferSize(1024 * 1024);
		this.socket.setSendBufferSize(1024 * 1024);
		
		this.dataInputStream = new DataInputStream(this.socket.getInputStream());
	}
	
	public PRemoteDroidAction receiveAction() throws IOException
	{
		synchronized (this.dataInputStream)
		{
			PRemoteDroidAction action = PRemoteDroidAction.parse(this.dataInputStream);
			return action;
		}
	}
	
	public void sendAction(PRemoteDroidAction action) throws IOException
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		action.toDataOutputStream(new DataOutputStream(baos));
		
		synchronized (this.socket.getOutputStream())
		{
			this.socket.getOutputStream().write(baos.toByteArray());
		}
	}
	
	public void close() throws IOException
	{
		this.socket.shutdownInput();
		this.socket.shutdownOutput();
		this.socket.close();
	}
	
	public InetAddress getInetAddress()
	{
		return this.socket.getInetAddress();
	}
	
	public int getPort()
	{
		return this.socket.getPort();
	}
}
