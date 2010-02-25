package org.pierre.remotedroid.server;

import java.util.HashMap;

public class UnicodeToSwingKeyCodeConverter
{
	public static final int NO_SWING_KEYCODE = -1;
	
	private static HashMap<Integer, Integer> map;
	private static final int[][] othersArray = {
	        {
	                10, 10
	        }, {
	                -1, 8
	        }, {
	                32, 32
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
		return (unicode >= 65 && unicode <= 90) || (unicode >= 48 && unicode <= 57);
	}
	
	private static void initMap()
	{
		initIntervals();
		initOthers();
	}
	
	private static void initIntervals()
	{
		initInterval(97, 122, 65);
		initInterval(65, 90, 65);
		initInterval(48, 57, 48);
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
