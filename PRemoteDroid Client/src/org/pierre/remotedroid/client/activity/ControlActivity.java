package org.pierre.remotedroid.client.activity;

import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.client.app.PRemoteDroid;
import org.pierre.remotedroid.protocol.action.MouseClickAction;
import org.pierre.remotedroid.protocol.action.MouseMoveAction;
import org.pierre.remotedroid.protocol.action.MouseWheelAction;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

public class ControlActivity extends Activity
{
	private static final int KEYBOARD_MENU_ITEM_ID = 0;
	private static final int FILE_EXPLORER_MENU_ITEM_ID = 1;
	private static final int SETTINGS_MENU_ITEM_ID = 2;
	private static final int GET_SERVER_MENU_ITEM_ID = 3;
	private static final int HELP__MENU_ITEM_ID = 4;
	
	private PRemoteDroid application;
	private SharedPreferences preferences;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.control);
		
		this.application = (PRemoteDroid) this.getApplication();
		
		this.preferences = this.application.getPreferences();
		
		this.checkOnCreate();
	}
	
	public boolean onTrackballEvent(MotionEvent event)
	{
		int amount = Math.round(event.getY() * 6);
		
		if (amount != 0)
		{
			MouseWheelAction action = new MouseWheelAction((byte) amount);
			this.application.sendAction(action);
		}
		
		return true;
	}
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(Menu.NONE, KEYBOARD_MENU_ITEM_ID, Menu.NONE, this.getResources().getString(R.string.text_keyboard));
		menu.add(Menu.NONE, FILE_EXPLORER_MENU_ITEM_ID, Menu.NONE, this.getResources().getString(R.string.text_file_explorer));
		menu.add(Menu.NONE, SETTINGS_MENU_ITEM_ID, Menu.NONE, this.getResources().getString(R.string.text_settings));
		menu.add(Menu.NONE, GET_SERVER_MENU_ITEM_ID, Menu.NONE, this.getResources().getString(R.string.text_get_server));
		menu.add(Menu.NONE, HELP__MENU_ITEM_ID, Menu.NONE, this.getResources().getString(R.string.text_help));
		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case KEYBOARD_MENU_ITEM_ID:
				this.toggleKeyboard();
				this.application.showToast(R.string.text_keyboard_not_supported);
				break;
			case FILE_EXPLORER_MENU_ITEM_ID:
				this.startActivity(new Intent(this, FileExplorerActivity.class));
				break;
			case SETTINGS_MENU_ITEM_ID:
				this.startActivity(new Intent(this, SettingsActivity.class));
				break;
			case GET_SERVER_MENU_ITEM_ID:
				this.startActivity(new Intent(this, GetServerActivity.class));
				break;
			case HELP__MENU_ITEM_ID:
				this.startActivity(new Intent(this, HelpActivity.class));
				break;
		}
		
		return true;
	}
	
	public void mouseClick(byte button, boolean state)
	{
		this.application.sendAction(new MouseClickAction(button, state));
	}
	
	public void mouseMove(int moveX, int moveY)
	{
		this.application.sendAction(new MouseMoveAction((short) moveX, (short) moveY));
	}
	
	private void toggleKeyboard()
	{
		InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, 0);
	}
	
	private void checkOnCreate()
	{
		if (this.checkFirstRun())
		{
			this.firstRunDialog();
		}
		else if (this.checkNewVersion())
		{
			this.newVersionDialog();
		}
	}
	
	private boolean checkFirstRun()
	{
		return this.preferences.getBoolean("debug_firstRun", true);
	}
	
	private void firstRunDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setMessage(R.string.text_first_run_dialog);
		builder.setPositiveButton(R.string.text_yes, new OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				ControlActivity.this.startActivity(new Intent(ControlActivity.this, HelpActivity.class));
				ControlActivity.this.disableFirstRun();
			}
		});
		builder.setNegativeButton(R.string.text_no, new OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.cancel();
				ControlActivity.this.disableFirstRun();
			}
		});
		builder.create().show();
	}
	
	private void disableFirstRun()
	{
		Editor editor = this.preferences.edit();
		editor.putBoolean("debug_firstRun", false);
		editor.commit();
		
		this.updateVersionCode();
	}
	
	private boolean checkNewVersion()
	{
		try
		{
			if (this.getPackageManager().getPackageInfo("org.pierre.remotedroid.client", PackageManager.GET_META_DATA).versionCode != this.preferences.getInt("app_versionCode", 0))
			{
				
				return true;
			}
		}
		catch (NameNotFoundException e)
		{
			this.application.debug(e);
		}
		
		return false;
	}
	
	private void newVersionDialog()
	{
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setCancelable(false);
		builder.setMessage(R.string.text_new_version_dialog);
		builder.setPositiveButton(R.string.text_get_server, new OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				ControlActivity.this.startActivity(new Intent(ControlActivity.this, GetServerActivity.class));
				ControlActivity.this.updateVersionCode();
			}
		});
		builder.setNegativeButton(R.string.text_no, new OnClickListener()
		{
			public void onClick(DialogInterface dialog, int which)
			{
				dialog.cancel();
				ControlActivity.this.updateVersionCode();
			}
		});
		builder.create().show();
	}
	
	private void updateVersionCode()
	{
		try
		{
			Editor editor = this.preferences.edit();
			editor.putInt("app_versionCode", this.getPackageManager().getPackageInfo("org.pierre.remotedroid.client", PackageManager.GET_META_DATA).versionCode);
			editor.commit();
		}
		catch (NameNotFoundException e)
		{
			this.application.debug(e);
		}
	}
}
