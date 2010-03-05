package org.pierre.remotedroid.client.connection;

import java.io.Serializable;

import org.pierre.remotedroid.client.activity.ConnectionEditActivity;
import org.pierre.remotedroid.protocol.PRemoteDroidConnection;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public abstract class Connection implements Comparable<Connection>, Serializable
{
	private String name;
	private String password;
	
	public Connection()
	{
		this.name = "";
		this.password = PRemoteDroidConnection.DEFAULT_PASSWORD;
	}
	
	public static Connection load(SharedPreferences preferences, ConnectionList list, int position)
	{
		Connection connection = null;
		
		String type = preferences.getString("connection_" + position + "_type", null);
		
		if (type.equals("wifi"))
		{
			connection = ConnectionWifi.load(preferences, position);
		}
		else if (type.equals("bluetooth"))
		{
			connection = ConnectionBluetooth.load(preferences, position);
		}
		
		connection.name = preferences.getString("connection_" + position + "_name", null);
		
		connection.password = preferences.getString("connection_" + position + "_password", null);
		
		return connection;
	}
	
	public void save(Editor editor, int position)
	{
		editor.putString("connection_" + position + "_name", this.name);
		
		editor.putString("connection_" + position + "_password", this.password);
	}
	
	public abstract void edit(Context context);
	
	protected void edit(Context context, Intent intent)
	{
		ConnectionEditActivity.connectionParam = this;
		context.startActivity(intent);
	}
	
	public String getName()
	{
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String getPassword()
	{
		return password;
	}
	
	public void setPassword(String password)
	{
		this.password = password;
	}
	
	public int compareTo(Connection c)
	{
		return this.name.compareTo(c.name);
	}
}
