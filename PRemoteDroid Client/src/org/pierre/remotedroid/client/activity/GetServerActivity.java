package org.pierre.remotedroid.client.activity;

import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;

import org.apache.http.HttpException;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestHandler;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;
import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.client.app.PRemoteDroid;
import org.pierre.remotedroid.protocol.PRemoteDroidConnection;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.widget.TextView;

public class GetServerActivity extends Activity implements Runnable, HttpRequestHandler
{
	private static String FILENAME = "/PRemoteDroid-Server.zip";
	
	private ServerSocket serverSocket;
	private HttpService httpService;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.getserver);
	}
	
	protected void onStart()
	{
		super.onStart();
		
		try
		{
			this.createServer();
			
			this.createURL();
			
			(new Thread(this)).start();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			
			this.finish();
		}
	}
	
	protected void onStop()
	{
		super.onStop();
		
		try
		{
			if (this.serverSocket != null)
			{
				this.serverSocket.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void run()
	{
		try
		{
			while (true)
			{
				Socket socket = this.serverSocket.accept();
				
				this.configureServerConnection(socket);
				
				DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
				conn.bind(socket, new BasicHttpParams());
				
				try
				{
					this.httpService.handleRequest(conn, new BasicHttpContext());
				}
				catch (HttpException e)
				{
					e.printStackTrace();
				}
				
				conn.close();
			}
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	
	public void handle(HttpRequest request, HttpResponse response, HttpContext context) throws HttpException, IOException
	{
		String uri = request.getRequestLine().getUri();
		if (uri.equals(FILENAME))
		{
			response.setStatusCode(HttpStatus.SC_OK);
			response.setEntity(new InputStreamEntity(this.getResources().openRawResource(R.raw.premotedroidserver), -1));
		}
		else
		{
			response.setStatusCode(HttpStatus.SC_TEMPORARY_REDIRECT);
			response.setHeader("Location", FILENAME);
		}
	}
	
	private void createServer() throws IOException
	{
		this.serverSocket = new ServerSocket(PRemoteDroidConnection.DEFAULT_PORT);
		
		BasicHttpProcessor httpproc = new BasicHttpProcessor();
		httpproc.addInterceptor(new ResponseDate());
		httpproc.addInterceptor(new ResponseServer());
		httpproc.addInterceptor(new ResponseContent());
		httpproc.addInterceptor(new ResponseConnControl());
		
		HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
		reqistry.register("*", this);
		
		HttpParams httpParams = new BasicHttpParams();
		httpParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000);
		httpParams.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE, 8 * 1024);
		httpParams.setBooleanParameter(CoreConnectionPNames.STALE_CONNECTION_CHECK, false);
		httpParams.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true);
		httpParams.setParameter(CoreProtocolPNames.ORIGIN_SERVER, "HttpComponents/1.1");
		
		this.httpService = new HttpService(httpproc, new DefaultConnectionReuseStrategy(), new DefaultHttpResponseFactory());
		this.httpService.setParams(httpParams);
		this.httpService.setHandlerResolver(reqistry);
	}
	
	private void createURL() throws SocketException, UnknownHostException
	{
		TextView urlView = (TextView) this.findViewById(R.id.getServerUrl);
		urlView.setText("");
		
		Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
		while (interfaces.hasMoreElements())
		{
			NetworkInterface currentInterface = interfaces.nextElement();
			
			Enumeration<InetAddress> addresses = currentInterface.getInetAddresses();
			
			while (addresses.hasMoreElements())
			{
				InetAddress currentAddress = addresses.nextElement();
				
				if (!currentAddress.isLoopbackAddress() && !(currentAddress instanceof Inet6Address))
				{
					urlView.append("http://" + currentAddress.getHostAddress() + ":" + this.serverSocket.getLocalPort() + "\n");
				}
			}
		}
	}
	
	private void configureServerConnection(Socket socket)
	{
		SharedPreferences preferences = ((PRemoteDroid) this.getApplication()).getPreferences();
		
		Editor editor = preferences.edit();
		System.out.println(socket.getInetAddress().getHostAddress());
		editor.putString("connection_server", socket.getInetAddress().getHostAddress());
		editor.commit();
	}
}
