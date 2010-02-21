package org.pierre.remotedroid.protocol;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;

import org.pierre.remotedroid.protocol.action.PRemoteDroidAction;

public class PRemoteDroidConnection
{
	public final static int DEFAULT_PORT = 64788;
	
	private Socket socket;
	
	public PRemoteDroidConnection(Socket socket) throws SocketException
	{
		this.socket = socket;
		this.socket.setPerformancePreferences(0, 2, 1);
		this.socket.setTcpNoDelay(true);
		this.socket.setReceiveBufferSize(1024 * 1024);
		this.socket.setSendBufferSize(1024 * 1024);
	}
	
	public PRemoteDroidAction receiveAction() throws IOException
	{
		synchronized (this.socket.getInputStream())
		{
			PRemoteDroidAction action = PRemoteDroidAction.parse(new DataInputStream(this.socket.getInputStream()));
			action.sender = (InetSocketAddress) this.socket.getRemoteSocketAddress();
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
}
