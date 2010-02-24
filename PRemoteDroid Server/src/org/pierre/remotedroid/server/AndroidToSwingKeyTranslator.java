package org.pierre.remotedroid.server;

import java.awt.event.KeyEvent;
import java.util.HashMap;

public class AndroidToSwingKeyTranslator
{
	private static HashMap<Integer, Integer> map;
	
	static
	{
		init();
	}
	
	public static int translate(int androidKey)
	{
		Integer i = map.get(androidKey);
		
		if (i != null)
		{
			return i;
		}
		else
		{
			return KeyEvent.CHAR_UNDEFINED;
		}
	}
	
	private static void init()
	{
		map = new HashMap<Integer, Integer>();
		
		initAlpha();
		initNumeric();
		initOther();
	}
	
	private static void initAlpha()
	{
		initInterval(29, 54, KeyEvent.VK_A);
	}
	
	private static void initNumeric()
	{
		initInterval(7, 16, KeyEvent.VK_NUMPAD0);
	}
	
	private static void initInterval(int start, int end, int baseKey)
	{
		int shift = baseKey - start;
		
		for (int i = start; i <= end; i++)
		{
			map.put(i, i + shift);
		}
	}
	
	private static void initOther()
	{
		map.put(59, KeyEvent.VK_SHIFT);
		map.put(62, KeyEvent.VK_SPACE);
		map.put(66, KeyEvent.VK_ENTER);
		map.put(67, KeyEvent.VK_BACK_SPACE);
	}
}
