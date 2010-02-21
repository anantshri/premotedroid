package org.pierre.remotedroid.client.activity;

import java.util.ArrayList;

import android.R;
import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class ExploreFilesActivity extends ListActivity
{
	private ArrayList<String> list;
	private ArrayAdapter<String> adapter;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.list = new ArrayList<String>();
		this.adapter = new ArrayAdapter<String>(this, R.layout.simple_list_item_1, this.list);
		this.setListAdapter(this.adapter);
	}
}
