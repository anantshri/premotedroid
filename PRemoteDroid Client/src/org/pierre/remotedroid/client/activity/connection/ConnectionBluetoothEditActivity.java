package org.pierre.remotedroid.client.activity.connection;

import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.client.connection.ConnectionBluetooth;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ConnectionBluetoothEditActivity extends ConnectionEditActivity implements OnClickListener
{
	private static final int ADDRESS_REQUEST_CODE = 0;
	
	private ConnectionBluetooth connection;
	
	private EditText address;
	private Button edit;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		this.setContentView(R.layout.connectionbluetoothedit);
		
		super.onCreate(savedInstanceState);
		
		this.connection = (ConnectionBluetooth) connectionParam;
		
		this.address = (EditText) this.findViewById(R.id.address);
		
		this.edit = (Button) this.findViewById(R.id.edit);
		this.edit.setOnClickListener(this);
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
	
	public void onClick(View v)
	{
		super.onClick(v);
		
		if (v == this.edit)
		{
			this.startActivityForResult(new Intent(this, BluetoothDevicesActivity.class), ADDRESS_REQUEST_CODE);
		}
	}
	
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);
		
		if (resultCode == RESULT_OK)
		{
			if (requestCode == ADDRESS_REQUEST_CODE)
			{
				this.connection.setAddress(data.getStringExtra("address"));
			}
		}
	}
}
