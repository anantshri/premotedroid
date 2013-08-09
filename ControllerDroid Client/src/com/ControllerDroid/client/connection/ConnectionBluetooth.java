package com.ControllerDroid.client.connection;

import java.io.IOException;

import com.ControllerDroid.client.activity.connection.ConnectionBluetoothEditActivity;
import com.ControllerDroid.client.app.ControllerDroid;
import com.ControllerDroid.protocol.ControllerDroidConnection;
import com.ControllerDroid.protocol.bluetooth.ControllerDroidConnectionBluetooth;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ConnectionBluetooth extends Connection
{
	private String address;
	
	public ConnectionBluetooth()
	{
		super();
		
		this.address = "";
	}
	
	public static ConnectionBluetooth load(SharedPreferences preferences, int position)
	{
		ConnectionBluetooth connection = new ConnectionBluetooth();
		
		connection.address = preferences.getString("connection_" + position + "_address", null);
		
		return connection;
	}
	
	public void save(Editor editor, int position)
	{
		super.save(editor, position);
		
		editor.putInt("connection_" + position + "_type", BLUETOOTH);
		
		editor.putString("connection_" + position + "_address", this.address);
	}
	
	public void edit(Context context)
	{
		Intent intent = new Intent(context, ConnectionBluetoothEditActivity.class);
		this.edit(context, intent);
	}
	
	public ControllerDroidConnection connect(ControllerDroid application) throws IOException
	{
		return ControllerDroidConnectionBluetooth.create(application, this.address);
	}
	
	public String getAddress()
	{
		return address;
	}
	
	public void setAddress(String address)
	{
		this.address = address;
	}
}
