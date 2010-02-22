package org.pierre.remotedroid.client.activity;

import java.util.ArrayList;
import java.util.Arrays;

import org.pierre.remotedroid.client.app.PRemoteDroid;
import org.pierre.remotedroid.protocol.PRemoteDroidActionReceiver;
import org.pierre.remotedroid.protocol.action.ExploreFileRequestAction;
import org.pierre.remotedroid.protocol.action.ExploreFileResponseAction;
import org.pierre.remotedroid.protocol.action.PRemoteDroidAction;

import android.R;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class ExploreFileActivity extends ListActivity implements PRemoteDroidActionReceiver, OnItemClickListener
{
	private PRemoteDroid application;
	
	private ArrayList<String> fileListString;
	private ArrayAdapter<String> adapter;
	
	private String currentDirectoryString;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.application = (PRemoteDroid) this.getApplication();
		
		this.fileListString = new ArrayList<String>();
		
		this.adapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_1, this.fileListString);
		
		this.setListAdapter(this.adapter);
		
		this.getListView().setOnItemClickListener(this);
		
		this.currentDirectoryString = "";
	}
	
	protected void onResume()
	{
		super.onResume();
		
		this.application.registerActionReceiver(this);
		
		this.sendExploreRequest("");
	}
	
	protected void onPause()
	{
		super.onPause();
		
		this.application.unregisterActionReceiver(this);
	}
	
	public void receiveAction(PRemoteDroidAction action)
	{
		if (action instanceof ExploreFileResponseAction)
		{
			ExploreFileResponseAction efra = (ExploreFileResponseAction) action;
			
			this.currentDirectoryString = efra.directory;
			
			this.fileListString.clear();
			this.fileListString.addAll(Arrays.asList(efra.files));
			
			this.runOnUiThread(new Runnable()
			{
				public void run()
				{
					ExploreFileActivity.this.adapter.notifyDataSetInvalidated();
					ExploreFileActivity.this.getListView().setSelection(0);
				}
			});
		}
	}
	
	private void sendExploreRequest(String fileString)
	{
		this.application.sendAction(new ExploreFileRequestAction(this.currentDirectoryString, fileString));
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		this.sendExploreRequest(this.fileListString.get(position));
	}
}
