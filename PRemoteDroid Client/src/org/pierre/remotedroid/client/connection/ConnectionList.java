package org.pierre.remotedroid.client.connection;

import java.util.ArrayList;
import java.util.Collections;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ConnectionList
{
	private ArrayList<Connection> connections;
	private SharedPreferences preferences;
	
	private Connection used;
	
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
		
		int position = this.preferences.getInt("connection_use", -1);
		if (position >= 0)
		{
			this.used = this.get(position);
		}
	}
	
	public void save()
	{
		Editor editor = this.preferences.edit();
		
		int count = this.connections.size();
		editor.putInt("connection_count", count);
		
		editor.putInt("connection_use", this.getUsedPosition());
		
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
	
	public Connection add(int type)
	{
		Connection connection = null;
		
		switch (type)
		{
			case Connection.WIFI:
				connection = new ConnectionWifi();
				break;
			case Connection.BLUETOOTH:
				connection = new ConnectionBluetooth();
				break;
		}
		
		this.connections.add(connection);
		
		if (this.connections.size() == 1)
		{
			this.used = connection;
		}
		
		return connection;
	}
	
	public void remove(int position)
	{
		Connection connection = this.connections.remove(position);
		
		if (connection == this.used)
		{
			this.used = null;
		}
	}
	
	public Connection get(int position)
	{
		return this.connections.get(position);
	}
	
	public int getCount()
	{
		return this.connections.size();
	}
	
	public void use(int position)
	{
		this.used = this.get(position);
	}
	
	public Connection getUsed()
	{
		return this.used;
	}
	
	public int getUsedPosition()
	{
		return this.connections.indexOf(this.used);
	}
}
