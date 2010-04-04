package org.pierre.remotedroid.client.bluetooth;

public class BluetoothChecker
{
	private static boolean available;
	
	static
	{
		try
		{
			Class.forName("android.bluetooth.BluetoothAdapter");
			
			available = true;
		}
		catch (ClassNotFoundException e)
		{
			available = false;
		}
	}
	
	public static boolean isBluetoohAvailable()
	{
		return available;
	}
}
