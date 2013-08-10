package com.ControllerDroid.client.activity.connection;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout.LayoutParams;

import com.ControllerDroid.client.R;
import com.ControllerDroid.client.connection.Connection;

public class ConnectionEditActivity extends Activity implements OnClickListener
{
	public static Connection connectionParam;
	
	private Connection connection;
	
	private Button save;
	
	private EditText name;
	private EditText password;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.connection = connectionParam;
		
		this.save = (Button) this.findViewById(R.id.save);
		this.save.setOnClickListener(this);
		if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.GINGERBREAD)
		{
			// Don't need the Save button on newer devices
			android.widget.LinearLayout.LayoutParams a = (LayoutParams) this.save.getLayoutParams();
			a.height = 0;
			this.save.setLayoutParams(a);
			this.save.forceLayout();
		}
		
		this.name = (EditText) this.findViewById(R.id.name);
		this.password = (EditText) this.findViewById(R.id.password);
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu)
	{
		// Inflate the menu items for use in the action bar
		android.view.MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.connection_edit_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item)
	{
		// Handle presses on the action bar items
		switch (item.getItemId())
		{
			case R.id.action_save:
				this.finish();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
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
		
		this.connection.setName(this.name.getText().toString());
		this.connection.setPassword(this.password.getText().toString());
	}
	
	public void onClick(View v)
	{
		if (v == this.save)
		{
			this.finish();
		}
	}
}
