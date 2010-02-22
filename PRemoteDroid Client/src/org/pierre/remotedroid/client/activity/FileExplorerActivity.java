package org.pierre.remotedroid.client.activity;

import java.util.ArrayList;
import java.util.Arrays;

import org.pierre.remotedroid.client.app.PRemoteDroid;
import org.pierre.remotedroid.protocol.PRemoteDroidActionReceiver;
import org.pierre.remotedroid.protocol.action.FileExploreRequestAction;
import org.pierre.remotedroid.protocol.action.FileExploreResponseAction;
import org.pierre.remotedroid.protocol.action.PRemoteDroidAction;

import android.R;
import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class FileExplorerActivity extends ListActivity implements PRemoteDroidActionReceiver, OnItemClickListener
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
		
		this.currentDirectoryString = "";
		
		this.adapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_1, this.fileListString);
		this.setListAdapter(this.adapter);
		
		this.getListView().setOnItemClickListener(this);
	}
	
	protected void onResume()
	{
		super.onResume();
		
		this.application.registerActionReceiver(this);
		
		this.sendFileExploreRequest("");
	}
	
	protected void onPause()
	{
		super.onPause();
		
		this.application.unregisterActionReceiver(this);
	}
	
	public void receiveAction(PRemoteDroidAction action)
	{
		if (action instanceof FileExploreResponseAction)
		{
			FileExploreResponseAction fera = (FileExploreResponseAction) action;
			
			this.currentDirectoryString = fera.directory;
			
			this.fileListString.clear();
			this.fileListString.addAll(Arrays.asList(fera.files));
			
			this.runOnUiThread(new Runnable()
			{
				public void run()
				{
					FileExplorerActivity.this.adapter.notifyDataSetInvalidated();
					FileExplorerActivity.this.getListView().setSelection(0);
				}
			});
		}
	}
	
	private void sendFileExploreRequest(String fileString)
	{
		this.application.sendAction(new FileExploreRequestAction(this.currentDirectoryString, fileString));
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		this.sendFileExploreRequest(this.fileListString.get(position));
	}
}
