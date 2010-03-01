package org.pierre.remotedroid.protocol;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

public class PRemoteDroidConnectionTcp extends PRemoteDroidConnection
{
	public final static int DEFAULT_PORT = 64788;
	
	private Socket socket;
	
	public PRemoteDroidConnectionTcp(Socket socket) throws IOException
	{
		super(socket.getInputStream(), socket.getOutputStream());
		
		this.socket = socket;
		this.socket.setPerformancePreferences(0, 2, 1);
		this.socket.setTcpNoDelay(true);
		this.socket.setReceiveBufferSize(1024 * 1024);
		this.socket.setSendBufferSize(1024 * 1024);
	}
	
	public InetAddress getInetAddress()
	{
		return this.socket.getInetAddress();
	}
	
	public int getPort()
	{
		return this.socket.getPort();
	}
	
	public void close() throws IOException
	{
		super.close();
		
		this.socket.shutdownInput();
		this.socket.shutdownOutput();
		this.socket.close();
	}
}
