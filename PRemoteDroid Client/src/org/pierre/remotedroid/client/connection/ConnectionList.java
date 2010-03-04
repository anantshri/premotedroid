package org.pierre.remotedroid.client.connection;

import java.util.ArrayList;
import java.util.Collections;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ConnectionList
{
	private ArrayList<Connection> connections;
	private SharedPreferences preferences;
	
	public ConnectionList(SharedPreferences preferences)
	{
		this.preferences = preferences;
		this.connections = new ArrayList<Connection>();
		
		this.load();
	}
	
	private void load()
	{
		int count = this.preferences.getInt("connection_count", 0);
		
		for (int i = 0; i < count; i++)
		{
			Connection connection = Connection.load(this.preferences, this, i);
			this.connections.add(connection);
		}
	}
	
	public void save()
	{
		Editor editor = this.preferences.edit();
		
		int count = this.connections.size();
		editor.putInt("connection_count", count);
		
		for (int i = 0; i < count; i++)
		{
			this.connections.get(i).save(editor, i);
		}
		
		editor.commit();
	}
	
	public void sort()
	{
		Collections.sort(this.connections);
	}
	
	public Connection add(String type)
	{
		Connection connection = null;
		
		if (type.equals("wifi"))
		{
			connection = new ConnectionWifi();
		}
		else if (type.equals("bluetooth"))
		{
			connection = new ConnectionBluetooth();
		}
		
		this.connections.add(connection);
		
		return connection;
	}
	
	public void remove(int position)
	{
		this.connections.remove(position);
	}
	
	public Connection get(int position)
	{
		return this.connections.get(position);
	}
	
	public int getCount()
	{
		return this.connections.size();
	}
}
