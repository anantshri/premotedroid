package org.pierre.remotedroid.client.app;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

import org.pierre.remotedroid.client.R;
import org.pierre.remotedroid.protocol.PRemoteDroidConnection;
import org.pierre.remotedroid.protocol.action.AuthentificationAction;
import org.pierre.remotedroid.protocol.action.PRemoteDroidAction;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class PRemoteDroid extends Application implements Runnable, PRemoteDroidActionReceiver
{
	private static final long CONNECTION_CLOSE_DELAY = 3000;
	
	private SharedPreferences preferences;
	private Vibrator vibrator;
	
	private PRemoteDroidConnection[] connection;
	
	private ArrayList<PRemoteDroidActionReceiver> actionReceiverList;
	
	private Handler handler;
	
	private CloseConnectionScheduler closeConnectionScheduler;
	
	public void onCreate()
	{
		super.onCreate();
		
		this.preferences = PreferenceManager.getDefaultSharedPreferences(this);
		PreferenceManager.setDefaultValues(this, R.xml.settings, true);
		
		this.vibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
		
		this.actionReceiverList = new ArrayList<PRemoteDroidActionReceiver>();
		
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
		this.vibrator.vibrate(l);
	}
	
	public synchronized void run()
	{
		PRemoteDroidConnection c = null;
		
		try
		{
			String server = this.preferences.getString("connection_server", null);
			int port = Integer.parseInt(this.preferences.getString("connection_port", null));
			String password = this.preferences.getString("connection_password", null);
			
			synchronized (this.connection)
			{
				c = new PRemoteDroidConnection(new Socket(server, port));
				
				this.connection[0] = c;
			}
			
			try
			{
				this.showToast(R.string.text_connection_established);
				
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
					
					c.close();
				}
			}
		}
		catch (IOException e)
		{
			this.debug(e);
			
			if (c == null)
			{
				this.showToast(R.string.text_connection_refused);
			}
			else
			{
				this.showToast(R.string.text_connection_closed);
			}
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
	
	public void showToast(final int resId)
	{
		if (this.actionReceiverList.size() > 0)
		{
			this.handler.post(new Runnable()
			{
				public void run()
				{
					Toast.makeText(PRemoteDroid.this, resId, Toast.LENGTH_SHORT).show();
				}
			});
		}
	}
	
	public void receiveAction(PRemoteDroidAction action)
	{
		synchronized (this.actionReceiverList)
		{
			for (PRemoteDroidActionReceiver actionReceiver : this.actionReceiverList)
			{
				actionReceiver.receiveAction(action);
			}
		}
	}
	
	public void registerActionReceiver(PRemoteDroidActionReceiver actionReceiver)
	{
		synchronized (this.actionReceiverList)
		{
			this.actionReceiverList.add(actionReceiver);
			
			if (this.actionReceiverList.size() > 0)
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
		synchronized (this.actionReceiverList)
		{
			this.actionReceiverList.remove(actionReceiver);
			
			if (this.actionReceiverList.size() == 0)
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
				
				synchronized (PRemoteDroid.this.connection)
				{
					if (PRemoteDroid.this.connection[0] != null)
					{
						synchronized (PRemoteDroid.this.actionReceiverList)
						{
							if (PRemoteDroid.this.actionReceiverList.size() == 0)
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
