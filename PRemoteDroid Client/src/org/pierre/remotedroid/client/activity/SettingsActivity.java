package org.pierre.remotedroid.client.activity;

import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.client.app.PRemoteDroid;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity
{
	private static final String[] tabFloatPreferences = { "control_sensitivity", "control_acceleration", "control_immobile_distance", "screenCapture_cursor_size" };
	private static final String[] tabIntPreferences = { "connection_port", "control_click_delay", "control_hold_delay" };
	
	private static final int resetPreferencesMenuItemId = 0;
	
	private SharedPreferences preferences;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.addPreferencesFromResource(R.xml.settings);
		
		this.preferences = ((PRemoteDroid) this.getApplication()).getPreferences();
	}
	
	protected void onPause()
	{
		super.onPause();
		
		this.checkPreferences();
	}
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(Menu.NONE, resetPreferencesMenuItemId, Menu.NONE, this.getResources().getString(R.string.text_reset_preferences));
		
		return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case resetPreferencesMenuItemId:
				this.resetPreferences();
				break;
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	private void checkPreferences()
	{
		Editor editor = this.preferences.edit();
		
		for (String s : tabFloatPreferences)
		{
			try
			{
				Float.parseFloat(this.preferences.getString(s, null));
			}
			catch (NumberFormatException e)
			{
				e.printStackTrace();
				editor.remove(s);
			}
		}
		
		for (String s : tabIntPreferences)
		{
			try
			{
				Integer.parseInt(this.preferences.getString(s, null));
			}
			catch (NumberFormatException e)
			{
				e.printStackTrace();
				editor.remove(s);
			}
		}
		
		editor.commit();
		
		PreferenceManager.setDefaultValues(this, R.xml.settings, true);
	}
	
	private void resetPreferences()
	{
		this.setPreferenceScreen(null);
		
		Editor editor = this.preferences.edit();
		editor.clear();
		editor.commit();
		
		PreferenceManager.setDefaultValues(this, R.xml.settings, true);
		
		this.addPreferencesFromResource(R.xml.settings);
	}
}
