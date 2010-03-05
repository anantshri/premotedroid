package org.pierre.remotedroid.client.activity;

import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.client.connection.ConnectionBluetooth;

import android.os.Bundle;
import android.widget.EditText;

public class ConnectionBluetoothEditActivity extends ConnectionEditActivity
{
	private ConnectionBluetooth connection;
	
	private EditText address;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		this.setContentView(R.layout.connectionbluetoothedit);
		
		super.onCreate(savedInstanceState);
		
		this.connection = (ConnectionBluetooth) connectionParam;
		
		this.address = (EditText) this.findViewById(R.id.address);
	}
	
	protected void onResume()
	{
		super.onResume();
		
		this.address.setText(this.connection.getAddress());
	}
	
	protected void onPause()
	{
		super.onPause();
		
		this.connection.setAddress(this.address.getText().toString());
	}
}
