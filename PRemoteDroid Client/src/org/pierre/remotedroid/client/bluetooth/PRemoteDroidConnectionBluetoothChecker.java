package org.pierre.remotedroid.client.bluetooth;

public class PRemoteDroidConnectionBluetoothChecker
{
	public static boolean isBluetoohAvailable()
	{
		try
		{
			Class.forName("android.bluetooth.BluetoothAdapter");
			
			return true;
		}
		catch (ClassNotFoundException e)
		{
			e.printStackTrace();
			
			return false;
		}
	}
}
