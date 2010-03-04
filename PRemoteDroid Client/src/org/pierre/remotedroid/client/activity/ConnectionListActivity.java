package org.pierre.remotedroid.client.activity;

import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.client.app.PRemoteDroid;
import org.pierre.remotedroid.client.connection.Connection;
import org.pierre.remotedroid.client.connection.ConnectionBluetooth;
import org.pierre.remotedroid.client.connection.ConnectionList;
import org.pierre.remotedroid.client.connection.ConnectionWifi;

import android.app.ListActivity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ConnectionListActivity extends ListActivity
{
	private PRemoteDroid application;
	private SharedPreferences preferences;
	
	private ConnectionList connections;
	
	private ConnectionListAdapter adapter;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.application = (PRemoteDroid) this.getApplication();
		
		this.preferences = this.application.getPreferences();
		
		this.connections = new ConnectionList(this.preferences);
		
		this.adapter = new ConnectionListAdapter(this, this.connections);
		
		this.setListAdapter(this.adapter);
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
				return 0;
			}
			else if (connection instanceof ConnectionBluetooth)
			{
				return 1;
			}
			
			return IGNORE_ITEM_VIEW_TYPE;
		}
		
		public int getViewTypeCount()
		{
			return 2;
		}
		
		public int getCount()
		{
			return this.connections.getCount();
		}
		
		public Object getItem(int position)
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
			
			if (convertView == null)
			{
				holder = new ConnectionViewHolder();
				
				if (connection instanceof ConnectionWifi)
				{
					convertView = this.layoutInflater.inflate(R.layout.connectionwifi, null);
					
					holder.address = (TextView) convertView.findViewById(R.id.hostPort);
				}
				else if (connection instanceof ConnectionBluetooth)
				{
					convertView = this.layoutInflater.inflate(R.layout.connectionbluetooth, null);
					
					holder.address = (TextView) convertView.findViewById(R.id.address);
				}
				
				holder.name = (TextView) convertView.findViewById(R.id.name);
				
				convertView.setTag(holder);
			}
			else
			{
				holder = (ConnectionViewHolder) convertView.getTag();
			}
			
			holder.name.setText(connection.getName());
			
			if (connection instanceof ConnectionWifi)
			{
				ConnectionWifi connectionWifi = (ConnectionWifi) connection;
				
				holder.hostPort.setText(connectionWifi.getHost() + ":" + connectionWifi.getPort());
			}
			else if (connection instanceof ConnectionBluetooth)
			{
				ConnectionBluetooth connectionBluetooth = (ConnectionBluetooth) connection;
				
				holder.address.setText(connectionBluetooth.getAddress());
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
