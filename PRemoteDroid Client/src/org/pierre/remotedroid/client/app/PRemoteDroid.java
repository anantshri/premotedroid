package org.pierre.remotedroid.client.app;

import java.io.IOException;
import java.util.HashSet;

import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.client.bluetooth.BluetoothChecker;
import org.pierre.remotedroid.protocol.PRemoteDroidActionReceiver;
import org.pierre.remotedroid.protocol.PRemoteDroidConnection;
import org.pierre.remotedroid.protocol.action.AuthentificationAction;
import org.pierre.remotedroid.protocol.action.PRemoteDroidAction;
import org.pierre.remotedroid.protocol.bluetooth.PRemoteDroidConnectionBluetooth;
import org.pierre.remotedroid.protocol.tcp.PRemoteDroidConnectionTcp;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class PRemoteDroid extends Application implements Runnable
{
	private static final long CONNECTION_CLOSE_DELAY = 3000;
	
	private SharedPreferences preferences;
	private Vibrator vibrator;
	
	private PRemoteDroidConnection[] connection;
	
	private HashSet<PRemoteDroidActionReceiver> actionReceivers;
	
	private Handler handler;
	
	private CloseConnectionScheduler closeConnectionScheduler;
	
	public void onCreate()
	{
		super.onCreate();
		
		this.preferences = PreferenceManager.getDefaultSharedPreferences(this);
		PreferenceManager.setDefaultValues(this, R.xml.settings, true);
		
		this.vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		
		this.actionReceivers = new HashSet<PRemoteDroidActionReceiver>();
		
		this.handler = new Handler();
		
		this.connection = new PRemoteDroidConnection[1];
		
		this.closeConnectionScheduler = new CloseConnectionScheduler();
	}
	
	public SharedPreferences getPreferences()
	{
		return this.preferences;
	}
	
	public void vibrate(long l)
	{
		if (this.preferences.getBoolean("feedback_vibration", true))
		{
			this.vibrator.vibrate(l);
		}
	}
	
	public synchronized void run()
	{
		PRemoteDroidConnection c = null;
		
		try
		{
			c = this.initConnection();
			
			synchronized (this.connection)
			{
				this.connection[0] = c;
			}
			
			try
			{
				this.showInternalToast(R.string.text_connection_established);
				
				String password = this.preferences.getString("connection_password", null);
				this.sendAction(new AuthentificationAction(password));
				
				while (true)
				{
					PRemoteDroidAction action = c.receiveAction();
					
					this.receiveAction(action);
				}
			}
			finally
			{
				synchronized (this.connection)
				{
					this.connection[0] = null;
				}
				
				c.close();
			}
		}
		catch (IOException e)
		{
			this.debug(e);
			
			if (c == null)
			{
				this.showInternalToast(R.string.text_connection_refused);
			}
			else
			{
				this.showInternalToast(R.string.text_connection_closed);
			}
		}
	}
	
	public PRemoteDroidConnection initConnection() throws IOException
	{
		String connectiontype = this.preferences.getString("connection_type", null);
		
		if (connectiontype.equals("wifi"))
		{
			return this.initConnectionTcp();
		}
		else if (connectiontype.equals("bluetooth"))
		{
			if (BluetoothChecker.isBluetoohAvailable())
			{
				return this.initConnectionBluetooth();
			}
		}
		
		throw new IOException();
	}
	
	public PRemoteDroidConnectionTcp initConnectionTcp() throws IOException
	{
		String server = this.preferences.getString("wifi_server", null);
		int port = Integer.parseInt(this.preferences.getString("wifi_port", null));
		
		PRemoteDroidConnectionTcp connection = PRemoteDroidConnectionTcp.create(server, port);
		
		return connection;
	}
	
	public PRemoteDroidConnectionBluetooth initConnectionBluetooth() throws IOException
	{
		String address = this.preferences.getString("bluetooth_device", null);
		
		try
		{
			PRemoteDroidConnectionBluetooth connection = PRemoteDroidConnectionBluetooth.create(address);
			
			return connection;
		}
		catch (IOException e)
		{
			this.showInternalToast(R.string.text_bluetooth_connection_error);
			
			throw e;
		}
	}
	
	public void sendAction(PRemoteDroidAction action)
	{
		synchronized (this.connection)
		{
			if (this.connection[0] != null)
			{
				try
				{
					this.connection[0].sendAction(action);
				}
				catch (IOException e)
				{
					this.debug(e);
				}
			}
		}
	}
	
	public void showInternalToast(int resId)
	{
		if (this.isInternalToast())
		{
			this.showToast(resId);
		}
	}
	
	public void showInternalToast(String message)
	{
		if (this.isInternalToast())
		{
			this.showToast(message);
		}
	}
	
	public boolean isInternalToast()
	{
		synchronized (this.actionReceivers)
		{
			return !this.actionReceivers.isEmpty();
		}
	}
	
	public void showToast(int resId)
	{
		this.showToast(this.getResources().getString(resId));
	}
	
	public void showToast(final String message)
	{
		this.handler.post(new Runnable()
		{
			public void run()
			{
				Toast.makeText(PRemoteDroid.this, message, Toast.LENGTH_LONG).show();
			}
		});
	}
	
	private void receiveAction(PRemoteDroidAction action)
	{
		synchronized (this.actionReceivers)
		{
			for (PRemoteDroidActionReceiver actionReceiver : this.actionReceivers)
			{
				actionReceiver.receiveAction(action);
			}
		}
	}
	
	public void registerActionReceiver(PRemoteDroidActionReceiver actionReceiver)
	{
		synchronized (this.actionReceivers)
		{
			this.actionReceivers.add(actionReceiver);
			
			if (this.actionReceivers.size() > 0)
			{
				synchronized (this.connection)
				{
					if (this.connection[0] == null)
					{
						(new Thread(this)).start();
					}
				}
			}
		}
	}
	
	public void unregisterActionReceiver(PRemoteDroidActionReceiver actionReceiver)
	{
		synchronized (this.actionReceivers)
		{
			this.actionReceivers.remove(actionReceiver);
			
			if (this.actionReceivers.size() == 0)
			{
				this.closeConnectionScheduler.schedule();
			}
		}
	}
	
	public void debug(Exception e)
	{
		if (this.preferences.getBoolean("debug_enabled", false))
		{
			Log.d(this.getResources().getString(R.string.app_name), null, e);
		}
	}
	
	private class CloseConnectionScheduler implements Runnable
	{
		private Thread currentThread;
		
		public synchronized void run()
		{
			try
			{
				this.wait(PRemoteDroid.CONNECTION_CLOSE_DELAY);
				
				synchronized (PRemoteDroid.this.actionReceivers)
				{
					if (PRemoteDroid.this.actionReceivers.size() == 0)
					{
						synchronized (PRemoteDroid.this.connection)
						{
							if (PRemoteDroid.this.connection[0] != null)
							{
								PRemoteDroid.this.connection[0].close();
								
								PRemoteDroid.this.connection[0] = null;
							}
						}
					}
				}
				
				this.currentThread = null;
			}
			catch (InterruptedException e)
			{
				PRemoteDroid.this.debug(e);
			}
			catch (IOException e)
			{
				PRemoteDroid.this.debug(e);
			}
		}
		
		public synchronized void schedule()
		{
			if (this.currentThread != null)
			{
				this.currentThread.interrupt();
			}
			
			this.currentThread = new Thread(this);
			
			this.currentThread.start();
		}
	}
}
