package org.pierre.remotedroid.server.tools;

import java.awt.event.KeyEvent;
import java.util.HashMap;

import org.pierre.remotedroid.protocol.action.KeyboardAction;

public class UnicodeToSwingKeyCodeConverter
{
	public static final int NO_SWING_KEYCODE = -1;
	
	private static HashMap<Integer, Integer> map;
	private static final int[][] othersArray = {
	        {
	                10, KeyEvent.VK_ENTER
	        }, {
	                KeyboardAction.UNICODE_BACKSPACE, KeyEvent.VK_BACK_SPACE
	        }, {
	                32, KeyEvent.VK_SPACE
	        }
	};
	
	static
	{
		map = new HashMap<Integer, Integer>();
		
		initMap();
	}
	
	public static int convert(int unicode)
	{
		Integer i = map.get(unicode);
		
		if (i != null)
		{
			return i;
		}
		else
		{
			return NO_SWING_KEYCODE;
		}
	}
	
	public static boolean useShift(int unicode)
	{
		return (unicode >= KeyEvent.VK_A && unicode <= KeyEvent.VK_Z) || (unicode >= KeyEvent.VK_0 && unicode <= KeyEvent.VK_9);
	}
	
	private static void initMap()
	{
		initIntervals();
		initOthers();
	}
	
	private static void initIntervals()
	{
		initInterval(97, 122, KeyEvent.VK_A);
		initInterval(65, 90, KeyEvent.VK_A);
		initInterval(48, 57, KeyEvent.VK_0);
	}
	
	private static void initInterval(int unicodeStart, int unicodeStop, int swingKeyCodeStart)
	{
		int shift = swingKeyCodeStart - unicodeStart;
		
		for (int i = unicodeStart; i <= unicodeStop; i++)
		{
			map.put(i, i + shift);
		}
	}
	
	private static void initOthers()
	{
		for (int i = 0; i < othersArray.length; i++)
		{
			map.put(othersArray[i][0], othersArray[i][1]);
		}
	}
}
