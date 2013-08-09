package com.ControllerDroid.server.connection;

import com.ControllerDroid.server.ControllerDroidServerApp;

public abstract class ControllerDroidServer
{
	protected ControllerDroidServerApp application;
	
	public ControllerDroidServer(ControllerDroidServerApp application)
	{
		this.application = application;
	}
	
	public abstract void close();
}
