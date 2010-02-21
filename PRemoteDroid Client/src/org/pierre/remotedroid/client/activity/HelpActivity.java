package org.pierre.remotedroid.client.activity;

import org.pierre.remotedroid.client.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class HelpActivity extends Activity
{
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.help);
		
		this.findViewById(R.id.getServerButton).setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(HelpActivity.this, GetServerActivity.class);
				HelpActivity.this.startActivity(intent);
			}
		});
		
		this.findViewById(R.id.settingsButton).setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				Intent intent = new Intent(HelpActivity.this, SettingsActivity.class);
				HelpActivity.this.startActivity(intent);
			}
		});
	}
}
