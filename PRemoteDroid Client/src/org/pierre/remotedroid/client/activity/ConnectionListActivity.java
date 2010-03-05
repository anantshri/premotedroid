package org.pierre.remotedroid.client.activity;

import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.client.app.PRemoteDroid;
import org.pierre.remotedroid.client.connection.Connection;
import org.pierre.remotedroid.client.connection.ConnectionBluetooth;
import org.pierre.remotedroid.client.connection.ConnectionList;
import org.pierre.remotedroid.client.connection.ConnectionWifi;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;

public class ConnectionListActivity extends ListActivity implements OnItemClickListener, OnItemLongClickListener, OnClickListener
{
	private static final int NEW_MENU_ITEM_ID = 0;
	
	private PRemoteDroid application;
	private SharedPreferences preferences;
	
	private ConnectionList connections;
	
	private ConnectionListAdapter adapter;
	
	private AlertDialog alertDialogNew;
	
	private AlertDialog alertDialogItemLongClick;
	
	private int itemLongClickPosition;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.application = (PRemoteDroid) this.getApplication();
		
		this.preferences = this.application.getPreferences();
		
		this.connections = new ConnectionList(this.preferences);
		
		this.adapter = new ConnectionListAdapter(this, this.connections);
		
		this.setListAdapter(this.adapter);
		
		this.getListView().setOnItemClickListener(this);
		
		this.getListView().setOnItemLongClickListener(this);
		
		this.initAlertDialogNew();
		
		this.initAlertDialogItemLongClick();
	}
	
	protected void onResume()
	{
		super.onResume();
		
		this.refresh();
	}
	
	protected void onPause()
	{
		super.onPause();
		
		this.connections.save();
	}
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(Menu.NONE, NEW_MENU_ITEM_ID, Menu.NONE, R.string.text_new);
		
		return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case NEW_MENU_ITEM_ID:
				this.alertDialogNew.show();
				break;
		}
		
		return true;
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
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
			Connection connection = this.connections.add(which);
			
			this.refresh();
			
			connection.edit(this);
		}
		else if (dialog == this.alertDialogItemLongClick)
		{
			Connection connection = this.connections.get(this.itemLongClickPosition);
			
			switch (which)
			{
				case 0:
					break;
				case 1:
					connection.edit(this);
					break;
				case 2:
					this.connections.remove(this.itemLongClickPosition);
					this.refresh();
					break;
			}
		}
	}
	
	private void refresh()
	{
		this.connections.sort();
		this.adapter.notifyDataSetChanged();
	}
	
	private void initAlertDialogNew()
	{
		String[] connectionTypeName = this.getResources().getStringArray(R.array.connection_type_name);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.text_connection_type);
		builder.setItems(connectionTypeName, this);
		this.alertDialogNew = builder.create();
	}
	
	private void initAlertDialogItemLongClick()
	{
		String[] connectionActionName = this.getResources().getStringArray(R.array.connection_action_name);
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setItems(connectionActionName, this);
		this.alertDialogItemLongClick = builder.create();
	}
	
	private class ConnectionListAdapter extends BaseAdapter
	{
		private ConnectionList connections;
		private LayoutInflater layoutInflater;
		
		public ConnectionListAdapter(Context context, ConnectionList connections)
		{
			this.connections = connections;
			
			this.layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}
		
		public int getItemViewType(int position)
		{
			Connection connection = this.connections.get(position);
			
			if (connection instanceof ConnectionWifi)
			{
				return Connection.WIFI;
			}
			else if (connection instanceof ConnectionBluetooth)
			{
				return Connection.BLUETOOTH;
			}
			
			return IGNORE_ITEM_VIEW_TYPE;
		}
		
		public int getViewTypeCount()
		{
			return Connection.TYPE_COUNT;
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
			
			Connection connection = this.connections.get(position);
			
			int type = this.getItemViewType(position);
			
			if (convertView == null)
			{
				holder = new ConnectionViewHolder();
				
				switch (type)
				{
					case Connection.WIFI:
						convertView = this.layoutInflater.inflate(R.layout.connectionwifi, null);
						holder.hostPort = (TextView) convertView.findViewById(R.id.hostPort);
						break;
					case Connection.BLUETOOTH:
						convertView = this.layoutInflater.inflate(R.layout.connectionbluetooth, null);
						holder.address = (TextView) convertView.findViewById(R.id.address);
						break;
				}
				
				holder.name = (TextView) convertView.findViewById(R.id.name);
				
				convertView.setTag(holder);
			}
			else
			{
				holder = (ConnectionViewHolder) convertView.getTag();
			}
			
			holder.name.setText(connection.getName());
			
			switch (type)
			{
				case Connection.WIFI:
					ConnectionWifi connectionWifi = (ConnectionWifi) connection;
					holder.hostPort.setText(connectionWifi.getHost() + ":" + connectionWifi.getPort());
					break;
				case Connection.BLUETOOTH:
					ConnectionBluetooth connectionBluetooth = (ConnectionBluetooth) connection;
					holder.address.setText(connectionBluetooth.getAddress());
					break;
			}
			
			return convertView;
		}
	}
	
	private class ConnectionViewHolder
	{
		public TextView name;
		public TextView hostPort;
		public TextView address;
	}
}
