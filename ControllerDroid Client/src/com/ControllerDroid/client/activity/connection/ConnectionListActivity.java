package com.ControllerDroid.client.activity.connection;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ControllerDroid.client.R;
import com.ControllerDroid.client.app.ControllerDroid;
import com.ControllerDroid.client.bluetooth.BluetoothChecker;
import com.ControllerDroid.client.connection.Connection;
import com.ControllerDroid.client.connection.ConnectionBluetooth;
import com.ControllerDroid.client.connection.ConnectionList;
import com.ControllerDroid.client.connection.ConnectionWifi;

public class ConnectionListActivity extends ListActivity implements OnItemClickListener, OnItemLongClickListener, OnClickListener
{
	private static final int NEW_MENU_ITEM_ID = 0;
	
	private ControllerDroid application;
	
	private ConnectionList connections;
	
	private ConnectionListAdapter adapter;
	
	private AlertDialog alertDialogNew;
	
	private AlertDialog alertDialogItemLongClick;
	
	private int itemLongClickPosition;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.application = (ControllerDroid) this.getApplication();
		
		this.connections = this.application.getConnections();
		
		this.adapter = new ConnectionListAdapter(this, this.connections);
		
		this.setListAdapter(this.adapter);
		
		this.getListView().setOnItemClickListener(this);
		
		this.getListView().setOnItemLongClickListener(this);
		
		this.init();
	}
	
	protected void onResume()
	{
		super.onResume();
		
		this.refresh();
		
		if (this.connections.getCount() == 0)
		{
			this.application.showToast(R.string.text_no_connection);
		}
	}
	
	protected void onPause()
	{
		super.onPause();
		
		this.connections.save();
	}
	
	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu)
	{
		// Inflate the menu items for use in the action bar
		android.view.MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.connection_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(android.view.MenuItem item)
	{
		// Handle presses on the action bar items
		switch (item.getItemId())
		{
			case R.id.action_new:
				this.alertDialogNew.show();
				return true;
			case R.id.action_edit:
				// Don't try to edit non-selected activity
				if (this.connections.getUsedPosition() < 0)
					return true;
				this.connections.get(this.connections.getUsedPosition()).edit(this);
				return true;
			case R.id.action_remove:
				// Don't try to remove non-selected activity
				if (this.connections.getUsedPosition() < 0)
					return true;
				this.connections.remove(this.connections.getUsedPosition());
				this.refresh();
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		this.useConnection(position);
	}
	
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
	{
		this.itemLongClickPosition = position;
		
		this.alertDialogItemLongClick.show();
		
		return true;
	}
	
	public void onClick(DialogInterface dialog, int which)
	{
		if (dialog == this.alertDialogNew)
		{
			this.addConnection(which);
		}
		else if (dialog == this.alertDialogItemLongClick)
		{
			this.menu(which);
		}
	}
	
	private void menu(int which)
	{
		Connection connection = this.connections.get(this.itemLongClickPosition);
		
		switch (which)
		{
			case 0:
				this.useConnection(this.itemLongClickPosition);
				break;
			case 1:
				connection.edit(this);
				break;
			case 2:
				this.removeConnection();
				break;
		}
	}
	
	private void addConnection(int which)
	{
		if (which == Connection.BLUETOOTH && !BluetoothChecker.isBluetoohAvailable())
		{
			this.application.showToast(R.string.text_bluetooth_not_available_version);
		}
		else
		{
			Connection connection = this.connections.add(which);
			
			this.refresh();
			
			connection.edit(this);
		}
	}
	
	private void useConnection(int position)
	{
		this.connections.use(position);
		this.refresh();
	}
	
	private void removeConnection()
	{
		this.connections.remove(this.itemLongClickPosition);
		this.refresh();
	}
	
	private void refresh()
	{
		this.connections.sort();
		this.adapter.notifyDataSetChanged();
	}
	
	private void init()
	{
		this.initAlertDialogNew();
		
		this.initAlertDialogItemLongClick();
	}
	
	private void initAlertDialogNew()
	{
		String[] connectionTypeName = {
		        this.getResources().getString(R.string.text_wifi), this.getResources().getString(R.string.text_bluetooth)
		};
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.text_connection_type);
		builder.setItems(connectionTypeName, this);
		this.alertDialogNew = builder.create();
	}
	
	private void initAlertDialogItemLongClick()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setItems(R.array.connection_action_name, this);
		this.alertDialogItemLongClick = builder.create();
	}
	
	private class ConnectionListAdapter extends BaseAdapter
	{
		private ConnectionList connections;
		private LayoutInflater layoutInflater;
		
		private int connectionUsedPosition;
		
		public ConnectionListAdapter(Context context, ConnectionList connections)
		{
			this.connections = connections;
			
			this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			
			this.connectionUsedPosition = this.connections.getUsedPosition();
		}
		
		public void notifyDataSetChanged()
		{
			super.notifyDataSetChanged();
			
			this.connectionUsedPosition = this.connections.getUsedPosition();
		}
		
		public int getCount()
		{
			return this.connections.getCount();
		}
		
		public Connection getItem(int position)
		{
			return this.connections.get(position);
		}
		
		public long getItemId(int position)
		{
			return position;
		}
		
		public View getView(int position, View convertView, ViewGroup parent)
		{
			ConnectionViewHolder holder;
			
			if (convertView == null)
			{
				holder = new ConnectionViewHolder();
				
				convertView = this.layoutInflater.inflate(R.layout.connection, null);
				
				holder.use = (RadioButton) convertView.findViewById(R.id.use);
				holder.icon = (ImageView) convertView.findViewById(R.id.icon);
				holder.name = (TextView) convertView.findViewById(R.id.name);
				
				convertView.setTag(holder);
			}
			else
			{
				holder = (ConnectionViewHolder) convertView.getTag();
			}
			
			Connection connection = this.connections.get(position);
			
			holder.use.setChecked(position == this.connectionUsedPosition);
			
			if (connection instanceof ConnectionWifi)
			{
				holder.icon.setImageResource(R.drawable.wifi);
			}
			else if (connection instanceof ConnectionBluetooth)
			{
				holder.icon.setImageResource(R.drawable.bluetooth);
			}
			
			holder.name.setText(connection.getName());
			
			return convertView;
		}
		
		private class ConnectionViewHolder
		{
			public RadioButton use;
			public ImageView icon;
			public TextView name;
		}
	}
}
