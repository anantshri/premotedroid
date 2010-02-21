package org.pierre.remotedroid.client.activity;

import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.client.app.PRemoteDroid;
import org.pierre.remotedroid.protocol.action.MouseClickAction;
import org.pierre.remotedroid.protocol.action.MouseMoveAction;
import org.pierre.remotedroid.protocol.action.MouseWheelAction;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;

public class ControlActivity extends Activity
{
	private static final int keyboardMenuItemId = 0;
	private static final int settingsMenuItemId = 1;
	private static final int getServerMenuItemId = 2;
	private static final int helpMenuItemId = 3;
	
	private PRemoteDroid application;
	
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		this.setContentView(R.layout.control);
		
		this.application = (PRemoteDroid) this.getApplication();
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
		menu.add(Menu.NONE, keyboardMenuItemId, Menu.NONE, this.getResources().getString(R.string.text_keyboard));
		menu.add(Menu.NONE, settingsMenuItemId, Menu.NONE, this.getResources().getString(R.string.text_settings));
		menu.add(Menu.NONE, getServerMenuItemId, Menu.NONE, this.getResources().getString(R.string.text_get_server));
		menu.add(Menu.NONE, helpMenuItemId, Menu.NONE, this.getResources().getString(R.string.text_help));
		
		return super.onCreateOptionsMenu(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case keyboardMenuItemId:
				this.toggleKeyboard();
				this.application.showToast(R.string.text_keyboard_not_supported);
				break;
			case settingsMenuItemId:
				this.startActivity(new Intent(this, SettingsActivity.class));
				break;
			case getServerMenuItemId:
				this.startActivity(new Intent(this, GetServerActivity.class));
				break;
			case helpMenuItemId:
				this.startActivity(new Intent(this, HelpActivity.class));
				break;
		}
		
		return super.onOptionsItemSelected(item);
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
}
