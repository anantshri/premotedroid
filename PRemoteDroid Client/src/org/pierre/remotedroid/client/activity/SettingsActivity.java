package org.pierre.remotedroid.client.activity;

import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.client.app.PRemoteDroid;
import org.pierre.remotedroid.client.bluetooth.BluetoothChecker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.view.Menu;
import android.view.MenuItem;

public class SettingsActivity extends PreferenceActivity
{
	private static final String[] tabFloatPreferences = {
	        "control_sensitivity", "control_acceleration", "control_immobile_distance", "screenCapture_cursor_size", "buttons_size", "wheel_bar_width"
	};
	private static final String[] tabIntPreferences = {
	        "wifi_port", "control_click_delay", "control_hold_delay"
	};
	
	private static final int resetPreferencesMenuItemId = 0;
	
	private PRemoteDroid application;
	private SharedPreferences preferences;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.addPreferencesFromResource(R.xml.settings);
		
		this.application = (PRemoteDroid) this.getApplication();
		this.preferences = this.application.getPreferences();
		
		this.init();
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
	
	private void init()
	{
		this.initConnectionType();
		this.initBluetooth();
	}
	
	private void initConnectionType()
	{
		ListPreference connectionType = (ListPreference) this.findPreference("connection_type");
		connectionType.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				if (newValue.equals("bluetooth") && !BluetoothChecker.isBluetoohAvailable())
				{
					return false;
				}
				else
				{
					return true;
				}
			}
		});
	}
	
	private void initBluetooth()
	{
		if (BluetoothChecker.isBluetoohAvailable())
		{
			this.initBluetoothDevice();
		}
		else
		{
			this.findPreference("bluetooth").setEnabled(false);
		}
	}
	
	private void initBluetoothDevice()
	{
		Preference bluetoothDevice = this.findPreference("bluetooth_device");
		bluetoothDevice.setOnPreferenceClickListener(new OnPreferenceClickListener()
		{
			public boolean onPreferenceClick(Preference preference)
			{
				SettingsActivity.this.startActivity(new Intent(SettingsActivity.this, BluetoothDevicesActivity.class));
				
				return true;
			}
		});
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
				this.application.debug(e);
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
				this.application.debug(e);
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
