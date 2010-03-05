package org.pierre.remotedroid.client.activity;

import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.client.connection.Connection;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

public class ConnectionEditActivity extends Activity
{
	private Connection connection;
	
	private EditText name;
	private EditText password;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.connection = (Connection) this.getIntent().getSerializableExtra("connection");
		
		this.name = (EditText) this.findViewById(R.id.name);
		this.password = (EditText) this.findViewById(R.id.password);
	}
	
	protected void onResume()
	{
		super.onResume();
		
		this.name.setText(this.connection.getName());
		this.password.setText(this.connection.getPassword());
	}
	
	protected void onPause()
	{
		super.onPause();
		
		System.out.println(this.name.getText().toString());
		
		this.connection.setName(this.name.getText().toString());
		this.connection.setPassword(this.password.getText().toString());
	}
}
