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
	private static final int KEYBOARD_MENU_ITEM_ID = 0;
	private static final int FILE_EXPLORER_MENU_ITEM_ID = 1;
	private static final int SETTINGS_MENU_ITEM_ID = 2;
	private static final int GET_SERVER_MENU_ITEM_ID = 3;
	private static final int HELP__MENU_ITEM_ID = 4;
	
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
		menu.add(Menu.NONE, KEYBOARD_MENU_ITEM_ID, Menu.NONE, this.getResources().getString(R.string.text_keyboard));
		menu.add(Menu.NONE, FILE_EXPLORER_MENU_ITEM_ID, Menu.NONE, this.getResources().getString(R.string.text_file_explorer));
		menu.add(Menu.NONE, SETTINGS_MENU_ITEM_ID, Menu.NONE, this.getResources().getString(R.string.text_settings));
		menu.add(Menu.NONE, GET_SERVER_MENU_ITEM_ID, Menu.NONE, this.getResources().getString(R.string.text_get_server));
		menu.add(Menu.NONE, HELP__MENU_ITEM_ID, Menu.NONE, this.getResources().getString(R.string.text_help));
		
		return super.onCreateOptionsMenu(menu);
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
