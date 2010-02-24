package org.pierre.remotedroid.client.keyboard;

import java.util.ArrayList;

import org.pierre.remotedroid.protocol.action.KeyboardAction;

import android.util.SparseIntArray;
import android.view.KeyEvent;

public class AndroidToSwingKeyConverter
{
	private SparseIntArray keyCodeMap;
	
	public AndroidToSwingKeyConverter()
	{
		this.keyCodeMap = new SparseIntArray();
		
		this.initKeyCodeMap();
	}
	
	public KeyboardAction[] convert(KeyEvent event)
	{
		ArrayList<KeyboardAction> actionList = new ArrayList<KeyboardAction>();
		
		int swingKeyCode = this.keyCodeMap.get(event.getKeyCode());
		
		if (swingKeyCode != 0)
		{
			actionList.add(new KeyboardAction(event.getAction() == KeyEvent.ACTION_DOWN, swingKeyCode));
		}
		
		KeyboardAction[] actionArray = new KeyboardAction[actionList.size()];
		actionArray = actionList.toArray(actionArray);
		
		return actionArray;
	}
	
	private void initKeyCodeMap()
	{
		this.initKeyCodeMapCharacters();
		this.initKeyCodeMapNumbers();
		this.initKeyCodeMapOthers();
	}
	
	private void initKeyCodeMapCharacters()
	{
		this.initKeyCodeMapInterval(KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_Z, 65);
	}
	
	private void initKeyCodeMapNumbers()
	{
		this.initKeyCodeMapInterval(KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_9, 96);
	}
	
	private void initKeyCodeMapInterval(int startKeyCode, int endKeyCode, int swingStartKeyCode)
	{
		int shift = swingStartKeyCode - startKeyCode;
		
		for (int i = startKeyCode; i <= endKeyCode; i++)
		{
			this.keyCodeMap.put(i, i + shift);
		}
	}
	
	private void initKeyCodeMapOthers()
	{
		
	}
}
