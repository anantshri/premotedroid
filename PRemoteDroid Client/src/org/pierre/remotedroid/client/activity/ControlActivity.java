package org.pierre.remotedroid.client.activity;

import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.client.app.PRemoteDroid;
import org.pierre.remotedroid.client.view.ControlView;
import org.pierre.remotedroid.protocol.PRemoteDroidActionReceiver;
import org.pierre.remotedroid.protocol.action.KeyboardAction;
import org.pierre.remotedroid.protocol.action.MouseClickAction;
import org.pierre.remotedroid.protocol.action.MouseMoveAction;
import org.pierre.remotedroid.protocol.action.MouseWheelAction;
import org.pierre.remotedroid.protocol.action.PRemoteDroidAction;
import org.pierre.remotedroid.protocol.action.ScreenCaptureResponseAction;

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
import android.content.res.Configuration;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

public class ControlActivity extends Activity implements PRemoteDroidActionReceiver
{
	private static final int FILE_EXPLORER_MENU_ITEM_ID = 0;
	private static final int KEYBOARD_MENU_ITEM_ID = 1;
	private static final int CONNECTIONS_MENU_ITEM_ID = 2;
	private static final int SETTINGS_MENU_ITEM_ID = 3;
	private static final int HELP_MENU_ITEM_ID = 4;
	
	private PRemoteDroid application;
	private SharedPreferences preferences;
	
	private ControlView controlView;
	
	private MediaPlayer mpClickOn;
	private MediaPlayer mpClickOff;
	
	private boolean feedbackSound;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.application = (PRemoteDroid) this.getApplication();
		
		this.preferences = this.application.getPreferences();
		
		this.checkFullscreen();
		
		this.setContentView(R.layout.control);
		
		this.setButtonsSize();
		
		this.controlView = (ControlView) this.findViewById(R.id.controlView);
		
		this.mpClickOn = MediaPlayer.create(this, R.raw.clickon);
		this.mpClickOff = MediaPlayer.create(this, R.raw.clickoff);
		
		this.checkOnCreate();
	}
	
	protected void onResume()
	{
		super.onResume();
		
		this.application.registerActionReceiver(this);
		
		this.feedbackSound = this.preferences.getBoolean("feedback_sound", false);
	}
	
	protected void onPause()
	{
		super.onPause();
		
		this.application.unregisterActionReceiver(this);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		int unicode = event.getUnicodeChar();
		
		if (unicode == 0 && event.getKeyCode() == KeyEvent.KEYCODE_DEL)
		{
			unicode = KeyboardAction.UNICODE_BACKSPACE;
		}
		
		if (unicode != 0)
		{
			this.application.sendAction(new KeyboardAction(unicode));
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(Menu.NONE, FILE_EXPLORER_MENU_ITEM_ID, Menu.NONE, this.getResources().getString(R.string.text_file_explorer));
		menu.add(Menu.NONE, KEYBOARD_MENU_ITEM_ID, Menu.NONE, this.getResources().getString(R.string.text_keyboard));
		menu.add(Menu.NONE, CONNECTIONS_MENU_ITEM_ID, Menu.NONE, this.getResources().getString(R.string.text_connections));
		menu.add(Menu.NONE, SETTINGS_MENU_ITEM_ID, Menu.NONE, this.getResources().getString(R.string.text_settings));
		menu.add(Menu.NONE, HELP_MENU_ITEM_ID, Menu.NONE, this.getResources().getString(R.string.text_help));
		
		return true;
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case FILE_EXPLORER_MENU_ITEM_ID:
				this.startActivity(new Intent(this, FileExplorerActivity.class));
				break;
			case KEYBOARD_MENU_ITEM_ID:
				this.toggleKeyboard();
				break;
			case CONNECTIONS_MENU_ITEM_ID:
				this.startActivity(new Intent(this, ConnectionListActivity.class));
				break;
			case SETTINGS_MENU_ITEM_ID:
				this.startActivity(new Intent(this, SettingsActivity.class));
				break;
			case HELP_MENU_ITEM_ID:
				this.startActivity(new Intent(this, HelpActivity.class));
				break;
		}
		
		return true;
	}
	
	public void receiveAction(PRemoteDroidAction action)
	{
		if (action instanceof ScreenCaptureResponseAction)
		{
			this.controlView.receiveAction((ScreenCaptureResponseAction) action);
		}
	}
	
	public void mouseClick(byte button, boolean state)
	{
		this.application.sendAction(new MouseClickAction(button, state));
		
		if (this.feedbackSound)
		{
			if (state)
			{
				this.playSound(this.mpClickOn);
			}
			else
			{
				this.playSound(this.mpClickOff);
			}
		}
	}
	
	public void mouseMove(int moveX, int moveY)
	{
		this.application.sendAction(new MouseMoveAction((short) moveX, (short) moveY));
	}
	
	public void mouseWheel(int amount)
	{
		this.application.sendAction(new MouseWheelAction((byte) amount));
	}
	
	private void playSound(MediaPlayer mp)
	{
		mp.seekTo(0);
		mp.start();
	}
	
	private void toggleKeyboard()
	{
		InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, 0);
	}
	
	private void checkFullscreen()
	{
		if (this.preferences.getBoolean("fullscreen", false))
		{
			this.requestWindowFeature(Window.FEATURE_NO_TITLE);
			this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
	}
	
	private void setButtonsSize()
	{
		LinearLayout clickLayout = (LinearLayout) this.findViewById(R.id.clickLayout);
		
		int orientation = this.getResources().getConfiguration().orientation;
		
		int size = (int) (Float.parseFloat(this.preferences.getString("buttons_size", null)) * this.getResources().getDisplayMetrics().density);
		
		if (orientation == Configuration.ORIENTATION_PORTRAIT)
		{
			clickLayout.getLayoutParams().height = (int) size;
		}
		else if (orientation == Configuration.ORIENTATION_LANDSCAPE)
		{
			clickLayout.getLayoutParams().width = (int) size;
		}
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
			if (this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_META_DATA).versionCode != this.preferences.getInt("app_versionCode", 0))
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
		builder.setNeutralButton(R.string.text_ok, new OnClickListener()
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
			editor.putInt("app_versionCode", this.getPackageManager().getPackageInfo(this.getPackageName(), PackageManager.GET_META_DATA).versionCode);
			editor.commit();
		}
		catch (NameNotFoundException e)
		{
			this.application.debug(e);
		}
	}
	
}
