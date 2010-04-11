package org.pierre.remotedroid.client.app;

import java.io.IOException;
import java.util.HashSet;

import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.client.connection.Connection;
import org.pierre.remotedroid.client.connection.ConnectionList;
import org.pierre.remotedroid.protocol.PRemoteDroidActionReceiver;
import org.pierre.remotedroid.protocol.PRemoteDroidConnection;
import org.pierre.remotedroid.protocol.action.AuthentificationAction;
import org.pierre.remotedroid.protocol.action.AuthentificationResponseAction;
import org.pierre.remotedroid.protocol.action.PRemoteDroidAction;

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
	
	private ConnectionList connections;
	
	private boolean requestEnableBluetooth;
	
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
		
		this.connections = new ConnectionList(this.preferences);
		
		this.requestEnableBluetooth = true;
	}
	
	public SharedPreferences getPreferences()
	{
		return this.preferences;
	}
	
	public ConnectionList getConnections()
	{
		return this.connections;
	}
	
	public void vibrate(long l)
	{
		if (this.preferences.getBoolean("feedback_vibration", true))
		{
			this.vibrator.vibrate(l);
		}
	}
	
	public boolean requestEnableBluetooth()
	{
		boolean b = this.requestEnableBluetooth;
		
		this.requestEnableBluetooth = false;
		
		return b;
	}
	
	public synchronized void run()
	{
		Connection co = this.connections.getUsed();
		
		if (co != null)
		{
			PRemoteDroidConnection c = null;
			
			try
			{
				c = co.connect(this);
				
				synchronized (this.connection)
				{
					this.connection[0] = c;
				}
				
				try
				{
					this.showInternalToast(R.string.text_connection_established);
					
					String password = co.getPassword();
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
		else
		{
			this.showInternalToast(R.string.text_no_connection_selected);
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
				Toast.makeText(PRemoteDroid.this, message, Toast.LENGTH_SHORT).show();
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
		
		if (action instanceof AuthentificationResponseAction)
		{
			this.receiveAuthentificationResponseAction((AuthentificationResponseAction) action);
		}
	}
	
	private void receiveAuthentificationResponseAction(AuthentificationResponseAction action)
	{
		if (action.authentificated)
		{
			this.showInternalToast(R.string.text_authentificated);
		}
		else
		{
			this.showInternalToast(R.string.text_not_authentificated);
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
