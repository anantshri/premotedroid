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
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView.OnItemClickListener;

public class FileExplorerActivity extends ListActivity implements PRemoteDroidActionReceiver, OnItemClickListener
{
	private PRemoteDroid application;
	
	private SharedPreferences preferences;
	
	private String directory;
	
	private ArrayList<String> files;
	private ArrayAdapter<String> adapter;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.application = (PRemoteDroid) this.getApplication();
		
		this.preferences = this.application.getPreferences();
		
		this.files = new ArrayList<String>();
		
		this.adapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_1, this.files);
		this.setListAdapter(this.adapter);
		
		this.getListView().setOnItemClickListener(this);
	}
	
	protected void onResume()
	{
		super.onResume();
		
		this.application.registerActionReceiver(this);
		
		this.directory = this.preferences.getString("fileExplore_directory", "");
		
		this.sendFileExploreRequest("");
	}
	
	protected void onPause()
	{
		super.onPause();
		
		this.application.unregisterActionReceiver(this);
		
		Editor editor = this.preferences.edit();
		editor.putString("fileExplore_directory", this.directory);
		editor.commit();
	}
	
	public void receiveAction(PRemoteDroidAction action)
	{
		if (action instanceof FileExploreResponseAction)
		{
			FileExploreResponseAction fera = (FileExploreResponseAction) action;
			
			this.directory = fera.directory;
			
			this.files.clear();
			this.files.addAll(Arrays.asList(fera.files));
			
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
		this.application.sendAction(new FileExploreRequestAction(this.directory, fileString));
	}
	
	public void onItemClick(AdapterView<?> parent, View view, int position, long id)
	{
		this.sendFileExploreRequest(this.files.get(position));
	}
}
