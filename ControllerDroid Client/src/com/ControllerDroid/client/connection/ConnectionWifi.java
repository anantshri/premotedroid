package com.ControllerDroid.client.connection;

import java.io.IOException;

import com.ControllerDroid.client.activity.connection.ConnectionWifiEditActivity;
import com.ControllerDroid.client.app.ControllerDroid;
import com.ControllerDroid.protocol.ControllerDroidConnection;
import com.ControllerDroid.protocol.tcp.ControllerDroidConnectionTcp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ConnectionWifi extends Connection
{
	private String host;
	private int port;
	
	public ConnectionWifi()
	{
		super();
		
		this.host = "";
		this.port = ControllerDroidConnectionTcp.DEFAULT_PORT;
	}
	
	public static ConnectionWifi load(SharedPreferences preferences, int position)
	{
		ConnectionWifi connection = new ConnectionWifi();
		
		connection.host = preferences.getString("connection_" + position + "_host", null);
		
		connection.port = preferences.getInt("connection_" + position + "_port", 0);
		
		return connection;
	}
	
	public void save(Editor editor, int position)
	{
		super.save(editor, position);
		
		editor.putInt("connection_" + position + "_type", WIFI);
		
		editor.putString("connection_" + position + "_host", this.host);
		
		editor.putInt("connection_" + position + "_port", this.port);
	}
	
	public void edit(Context context)
	{
		Intent intent = new Intent(context, ConnectionWifiEditActivity.class);
		this.edit(context, intent);
	}
	
	public ControllerDroidConnection connect(ControllerDroid application) throws IOException
	{
		return ControllerDroidConnectionTcp.create(this.host, this.port);
	}
	
	public String getHost()
	{
		return host;
	}
	
	public void setHost(String host)
	{
		this.host = host;
	}
	
	public int getPort()
	{
		return port;
	}
	
	public void setPort(int port)
	{
		this.port = port;
	}
}
