package org.pierre.remotedroid.client.activity;

import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.client.app.PRemoteDroid;
import org.pierre.remotedroid.client.connection.Connection;
import org.pierre.remotedroid.client.connection.ConnectionList;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
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
	
	private ConnectionList connections;
	
	private ConnectionListAdapter adapter;
	
	private AlertDialog alertDialogNew;
	
	private AlertDialog alertDialogItemLongClick;
	
	private int itemLongClickPosition;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.connections = ((PRemoteDroid) this.getApplication()).getConnections();
		
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
			this.onClickAlertDialogNew(which);
		}
		else if (dialog == this.alertDialogItemLongClick)
		{
			this.onClickAlertDialogItemLongClick(which);
		}
	}
	
	private void onClickAlertDialogNew(int which)
	{
		Connection connection = this.connections.add(which);
		
		this.refresh();
		
		connection.edit(this);
	}
	
	private void onClickAlertDialogItemLongClick(int which)
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
				
				holder.name = (TextView) convertView.findViewById(R.id.name);
				
				convertView.setTag(holder);
			}
			else
			{
				holder = (ConnectionViewHolder) convertView.getTag();
			}
			
			Connection connection = this.connections.get(position);
			
			holder.name.setText(connection.getName());
			if (position == this.connectionUsedPosition)
			{
				holder.name.setTextSize(30);
			}
			else
			{
				holder.name.setTextSize(20);
			}
			
			return convertView;
		}
	}
	
	private class ConnectionViewHolder
	{
		public TextView name;
	}
}
