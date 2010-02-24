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
		
		System.out.println(androidKey);
		
		if (i != null)
		{
			return i;
		}
		else
		{
			return -1;
		}
	}
	
	private static void init()
	{
		map = new HashMap<Integer, Integer>();
		
		initChar();
	}
	
	private static void initChar()
	{
		int shift = KeyEvent.VK_A - 29;
		
		for (int i = 29; i <= 54; i++)
		{
			map.put(i, i + shift);
		}
	}
}
